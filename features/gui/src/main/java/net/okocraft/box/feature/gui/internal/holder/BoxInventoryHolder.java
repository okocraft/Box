package net.okocraft.box.feature.gui.internal.holder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.event.MenuClickEvent;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public class BoxInventoryHolder implements InventoryHolder {

    private final Menu menu;
    private final PlayerSession session;
    private final Inventory inventory;
    private final ItemStack[] icons;
    private final Int2ObjectMap<Button> buttonMap;

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

    public void processClick(int slot, @NotNull ClickType clickType, @NotNull Consumer<UUID> onClickProcessed) {
        try {
            processClick0(slot, clickType, onClickProcessed);
        } catch (Throwable e) {
            var viewer = session.getViewer();
            viewer.sendMessage(Displays.ERROR_WHILE_CLICK_PROCESSING.apply(e));
            BoxLogger.logger().error("An error occurred while processing a click event ({})", viewer.getName(), e);

            BoxProvider.get().getScheduler().runEntityTask(viewer, () -> {
                viewer.closeInventory();
                onClickProcessed.accept(viewer.getUniqueId());
            });
        }
    }

    private void processClick0(int slot, @NotNull ClickType clickType, @NotNull Consumer<UUID> onClickProcessed) {
        var button = buttonMap.get(slot);

        if (button == null) {
            return;
        }

        var event = new MenuClickEvent(menu, session, button, clickType);

        BoxProvider.get().getEventManager().call(event);

        if (event.isCancelled()) {
            return;
        }

        var result = button.onClick(session, clickType);

        if (result instanceof ClickResult.WaitingTask waitingTask) {
            waitingTask.onCompleted(r -> processClickResult(button, r, onClickProcessed));
        } else {
            processClickResult(button, result, onClickProcessed);
        }
    }

    private void processClickResult(@NotNull Button button, @NotNull ClickResult clickResult, @NotNull Consumer<UUID> onClickProcessed) {
        if (clickResult instanceof ClickResult.WaitingTask) {
            throw new IllegalStateException("Nested waiting task");
        }

        if (clickResult == ClickResult.UPDATE_ICONS) {
            renderButtons();
            inventory.setContents(icons);
            onClickProcessed.accept(session.getViewer().getUniqueId());
        } else if (clickResult == ClickResult.UPDATE_BUTTON) {
            inventory.setItem(button.getSlot(), button.createIcon(session));
            onClickProcessed.accept(session.getViewer().getUniqueId());
        } else if (clickResult instanceof ClickResult.ChangeMenu changeMenu) {
            session.rememberMenu(menu);
            MenuOpener.open(changeMenu.menu(), session, onClickProcessed);
        }
    }

    private void renderButtons() {
        for (var button : menu.getButtons(session)) {
            int slot = button.getSlot();

            if (slot < icons.length) {
                buttonMap.put(slot, button);
                icons[slot] = button.createIcon(session);
            }
        }
    }
}
