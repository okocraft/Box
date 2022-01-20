package net.okocraft.box.feature.begui.internal.listener;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.begui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.begui.internal.lang.Displays;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class InventoryListener implements Listener {

    private final Map<UUID, CompletableFuture<?>> clickTaskMap = new HashMap<>();
    private final Map<UUID, Long> lastClickTime = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(@NotNull InventoryClickEvent event) {
        var inventoryView = event.getView();

        if (inventoryView.getTopInventory().getHolder() instanceof BoxInventoryHolder) {
            event.setCancelled(true);
        }

        var clicked = event.getClickedInventory();

        if (clicked == null || !(clicked.getHolder() instanceof BoxInventoryHolder holder)) {
            return;
        }

        event.setCancelled(true);

        var clicker = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());

        if (clicker == null) {
            return;
        }

        var currentTask = clickTaskMap.get(clicker.getUniqueId());

        if (currentTask != null && !currentTask.isDone()) {
            return;
        }

        var lastClickTime = this.lastClickTime.get(clicker.getUniqueId());

        if (lastClickTime != null && System.currentTimeMillis() - lastClickTime < 150) {
            return;
        }

        this.lastClickTime.put(clicker.getUniqueId(), System.currentTimeMillis());

        var task =
                BoxProvider.get().getTaskFactory()
                        .runAsync(() -> processClick(holder, clicker, event.getSlot(), event.getClick()))
                        .exceptionallyAsync(e -> reportError(clicker, e));

        clickTaskMap.put(clicker.getUniqueId(), task);
    }

    private void processClick(@NotNull BoxInventoryHolder holder, @NotNull Player clicker,
                              int slot, @NotNull ClickType type) {
        holder.processClick(clicker, slot, type);

        if (holder.updateMenu(clicker)) {
            BoxProvider.get().getTaskFactory().run(() -> holder.updateInventory(clicker)).join();
        }
    }

    private @Nullable Void reportError(@NotNull Player player, @NotNull Throwable throwable) {
        player.sendMessage(Displays.ERROR_WHILE_CLICK_PROCESSING.apply(throwable));

        BoxProvider.get().getLogger().log(
                Level.SEVERE,
                "An error occurred while processing a click event (" + player.getName() + ")",
                throwable
        );

        return null;
    }
}
