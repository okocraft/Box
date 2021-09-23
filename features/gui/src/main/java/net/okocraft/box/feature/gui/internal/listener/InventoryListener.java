package net.okocraft.box.feature.gui.internal.listener;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

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

        var task = CompletableFuture.runAsync(
                () -> processClick(holder, clicker, event.getSlot(), event.getClick()),
                BoxProvider.get().getExecutorProvider().getExecutor()
        ).exceptionallyAsync(throwable -> {
            BoxProvider.get().getLogger().log(
                    Level.SEVERE,
                    "Could not complete click task (" + clicker.getName() + ")",
                    throwable
            );
            return null;
        });

        clickTaskMap.put(clicker.getUniqueId(), task);
    }

    private void processClick(@NotNull BoxInventoryHolder holder, @NotNull Player clicker,
                              int slot, @NotNull ClickType type) {
        holder.processClick(clicker, slot, type);

        if (holder.updateMenu(clicker)) {
            CompletableFuture.runAsync(
                    () -> holder.updateInventory(clicker),
                    BoxProvider.get().getExecutorProvider().getMainThread()
            ).join();
        }
    }
}
