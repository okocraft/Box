package net.okocraft.box.feature.gui.internal.listener;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import net.okocraft.box.feature.gui.internal.menu.CategoryMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class InventoryListener implements Listener {

    private final Map<UUID, CompletableFuture<?>> clickTaskMap = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastClickTime = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        PlayerSession.unload(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(@NotNull InventoryClickEvent event) {
        var inventoryView = event.getView();

        if (inventoryView.getTopInventory().getHolder() instanceof BoxInventoryHolder topHolder) {
            event.setCancelled(true);
        } else {
            return;
        }

        var clicked = event.getClickedInventory();

        if (clicked == null) {
            return;
        }

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

        if (!(clicked.getHolder() instanceof BoxInventoryHolder holder)) {
            openCategoryMenu(clicker, topHolder.getMenu(), clicked.getItem(event.getSlot()));
            return;
        }

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
            BoxProvider.get().getTaskFactory().runEntityTask(clicker, holder::updateInventory).join();
        }
    }

    private void openCategoryMenu(@NotNull Player player, @NotNull Menu currentMenu, @Nullable ItemStack item) {
        var boxItem = item != null ? BoxProvider.get().getItemManager().getBoxItem(item).orElse(null) : null;

        if (boxItem == null) {
            return;
        }

        var category = findCategory(boxItem).orElse(null);

        if (category == null) {
            return;
        }

        var menu = new CategoryMenu(category);
        int page = category.getItems().indexOf(boxItem) / menu.getIconsPerPage() + 1;

        if (currentMenu instanceof CategoryMenu categoryMenu && categoryMenu.getCategory() == category && categoryMenu.getCurrentPage() == page) { // Same menu, and same page.
            return;
        }

        menu.setPage(page);
        MenuOpener.open(menu, player, false);
    }

    private @NotNull Optional<Category> findCategory(@NotNull BoxItem item) {
        return CategoryRegistry.get().values().stream()
                .filter(category -> category.containsItem(item))
                .findFirst();
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
