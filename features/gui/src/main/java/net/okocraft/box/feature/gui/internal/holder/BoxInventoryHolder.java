package net.okocraft.box.feature.gui.internal.holder;

import dev.siroshun.mcmsgdef.MessageKey;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.event.MenuClickEvent;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.MenuHistoryHolder;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class BoxInventoryHolder implements InventoryHolder {

    private static final String ERROR_KEY = "box.gui.click-error";
    private static final MessageKey.Arg1<Throwable> ERROR = MessageKey.arg1(ERROR_KEY, Placeholders.ERROR);

    // In Folia, some Inventory#getHolder implementation checks if the current thread is a correct tick thread, and it may fail.
    // Before calling Inventory#getHolder, we need to check if the inventory is CraftInventoryCustom, which does not check the thread.
    private static Class<?> craftInventoryCustomClass;

    public static void addDefaultErrorMessage(@NotNull DefaultMessageCollector collector) {
        collector.add(ERROR_KEY, "<red>An error occurred while click process. Error message: <white><error>");
    }

    // Initialize later to avoid calling Bukkit#getServer in the test environment.
    public static void initializeCraftInventoryCustomClass() {
        craftInventoryCustomClass = Bukkit.createInventory(null, 54, Component.empty()).getClass();
    }

    public static boolean isBoxMenu(@Nullable Inventory inventory) {
        return getFromInventory(inventory) != null;
    }

    public static @Nullable BoxInventoryHolder getFromInventory(@Nullable Inventory inventory) {
        return craftInventoryCustomClass.isInstance(inventory) && inventory.getHolder() instanceof BoxInventoryHolder holder ? holder : null;
    }

    private final Menu menu;
    private final PlayerSession session;
    private final int maxIcons;
    private final Inventory inventory;
    private final Int2ObjectMap<Button> buttonMap;
    private final AtomicBoolean isClickProcessing = new AtomicBoolean(false);
    private long lastClickTime;

    public BoxInventoryHolder(@NotNull Menu menu, @NotNull PlayerSession session) {
        this.menu = menu;
        this.session = session;
        this.maxIcons = menu.getRows() * 9;

        this.inventory = Bukkit.createInventory(this, this.maxIcons, menu.getTitle(session));
        this.buttonMap = new Int2ObjectOpenHashMap<>(this.maxIcons);

        this.renderButtons();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public @NotNull PlayerSession getSession() {
        return this.session;
    }

    public @NotNull Menu getMenu() {
        return this.menu;
    }

    public long getLastClickTime() {
        return this.lastClickTime;
    }

    public void updateLastClickTime() {
        this.lastClickTime = System.nanoTime();
    }

    public boolean tryStartClickProcess() {
        return this.isClickProcessing.compareAndSet(false, true);
    }

    public void finishClickProcess() {
        this.isClickProcessing.set(false);
    }

    public void processClick(int slot, @NotNull ClickType clickType) {
        try {
            this.processClick0(slot, clickType);
        } catch (Throwable e) {
            Player viewer = this.session.getViewer();
            viewer.sendMessage(ERROR.apply(e));
            BoxLogger.logger().error("An error occurred while processing a click event ({})", viewer.getName(), e);

            BoxAPI.api().getScheduler().runEntityTask(viewer, viewer::closeInventory);
            this.finishClickProcess();
        }
    }

    private void processClick0(int slot, @NotNull ClickType clickType) {
        var button = this.buttonMap.get(slot);

        if (button == null) {
            this.finishClickProcess();
            return;
        }

        var event = new MenuClickEvent(this.menu, this.session, button, clickType);

        BoxAPI.api().getEventCallers().sync().call(event);

        if (event.isCancelled()) {
            this.finishClickProcess();
            return;
        }

        var result = button.onClick(this.session, clickType);

        if (result instanceof ClickResult.WaitingTask waitingTask) {
            waitingTask.onCompleted(r -> this.processClickResult(button, r));
        } else {
            this.processClickResult(button, result);
        }
    }

    private void processClickResult(@NotNull Button button, @NotNull ClickResult clickResult) {
        if (clickResult instanceof ClickResult.WaitingTask) {
            throw new IllegalStateException("Nested waiting task");
        }

        if (clickResult == ClickResult.UPDATE_ICONS) {
            this.renderButtons();
        } else if (clickResult == ClickResult.UPDATE_BUTTON) {
            this.inventory.setItem(button.getSlot(), button.createIcon(this.session));
        } else if (clickResult == ClickResult.BACK_MENU) {
            MenuOpener.open(MenuHistoryHolder.getFromSession(this.session).backMenu(), this.session);
        } else if (clickResult instanceof ClickResult.ChangeMenu changeMenu) {
            MenuHistoryHolder.getFromSession(this.session).rememberMenu(this.menu);
            MenuOpener.open(changeMenu.menu(), this.session);
        }
        this.finishClickProcess();
    }

    public void renderButtons() {
        var icons = new ItemStack[this.maxIcons];
        this.buttonMap.clear();

        for (var button : this.menu.getButtons(this.session)) {
            int slot = button.getSlot();

            if (slot < icons.length) {
                this.buttonMap.put(slot, button);
                icons[slot] = button.createIcon(this.session);
            }
        }

        this.inventory.setContents(icons);
    }
}
