package net.okocraft.box.feature.gui.internal.holder;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class BoxInventoryHolder implements InventoryHolder {

    private static final String ERROR_KEY = "box.gui.click-error";
    private static final Arg1<Throwable> ERROR = Arg1.arg1(ERROR_KEY, Placeholders.ERROR);

    public static void addDefaultErrorMessage(@NotNull DefaultMessageCollector collector) {
        collector.add(ERROR_KEY, "<red>An error occurred while click process. Error message: <white><error>");
    }

    private final Menu menu;
    private final PlayerSession session;
    private final Inventory inventory;
    private final ItemStack[] icons;
    private final Int2ObjectMap<Button> buttonMap;
    private final AtomicBoolean isClickProcessing = new AtomicBoolean(false);
    private long lastClickTime;

    public BoxInventoryHolder(@NotNull Menu menu, @NotNull PlayerSession session) {
        this.menu = menu;
        this.session = session;

        int maxIcons = menu.getRows() * 9;

        this.inventory = Bukkit.createInventory(this, maxIcons, menu.getTitle(session));
        this.icons = new ItemStack[maxIcons];
        this.buttonMap = new Int2ObjectOpenHashMap<>(maxIcons);

        renderButtons();
        inventory.setContents(icons);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public @NotNull PlayerSession getSession() {
        return session;
    }

    public @NotNull Menu getMenu() {
        return menu;
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
            processClick0(slot, clickType);
        } catch (Throwable e) {
            var viewer = session.getViewer();
            ERROR.apply(e).source(this.session.getMessageSource()).send(viewer);
            BoxLogger.logger().error("An error occurred while processing a click event ({})", viewer.getName(), e);

            BoxAPI.api().getScheduler().runEntityTask(viewer, viewer::closeInventory);
            this.finishClickProcess();
        }
    }

    private void processClick0(int slot, @NotNull ClickType clickType) {
        var button = buttonMap.get(slot);

        if (button == null) {
            this.finishClickProcess();
            return;
        }

        var event = new MenuClickEvent(menu, session, button, clickType);

        BoxAPI.api().getEventManager().call(event);

        if (event.isCancelled()) {
            this.finishClickProcess();
            return;
        }

        var result = button.onClick(session, clickType);

        if (result instanceof ClickResult.WaitingTask waitingTask) {
            waitingTask.onCompleted(r -> processClickResult(button, r));
        } else {
            processClickResult(button, result);
        }
    }

    private void processClickResult(@NotNull Button button, @NotNull ClickResult clickResult) {
        if (clickResult instanceof ClickResult.WaitingTask) {
            throw new IllegalStateException("Nested waiting task");
        }

        if (clickResult == ClickResult.UPDATE_ICONS) {
            renderButtons();
            inventory.setContents(icons);
        } else if (clickResult == ClickResult.UPDATE_BUTTON) {
            inventory.setItem(button.getSlot(), button.createIcon(session));
        } else if (clickResult == ClickResult.BACK_MENU) {
            MenuOpener.open(MenuHistoryHolder.getFromSession(this.session).backMenu(), this.session);
        } else if (clickResult instanceof ClickResult.ChangeMenu changeMenu) {
            MenuHistoryHolder.getFromSession(this.session).rememberMenu(changeMenu.menu());
            MenuOpener.open(changeMenu.menu(), this.session);
        }
        this.finishClickProcess();
    }

    private void renderButtons() {
        Arrays.fill(this.icons, null);

        for (var button : menu.getButtons(session)) {
            int slot = button.getSlot();

            if (slot < icons.length) {
                buttonMap.put(slot, button);
                icons[slot] = button.createIcon(session);
            }
        }
    }
}
