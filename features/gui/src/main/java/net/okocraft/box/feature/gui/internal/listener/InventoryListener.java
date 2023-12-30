package net.okocraft.box.feature.gui.internal.listener;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.gui.internal.menu.CategoryMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class InventoryListener implements Listener {

    private static final long CLICK_COOLDOWN = TimeUnit.MILLISECONDS.toNanos(150);


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(@NotNull InventoryClickEvent event) {
        var inventoryView = event.getView();

        if (inventoryView.getTopInventory().getHolder() instanceof BoxInventoryHolder topHolder) {
            event.setCancelled(true);
        } else {
            return;
        }

        var clicked = event.getClickedInventory();

        if (clicked == null || System.nanoTime() - topHolder.getLastClickTime() < CLICK_COOLDOWN) {
            return;
        }

        topHolder.updateLastClickTime();

        if (!topHolder.tryStartClickProcess()) {
            return;
        }

        Runnable task;

        if (clicked.getHolder() instanceof BoxInventoryHolder holder) {
            task = () -> holder.processClick(event.getSlot(), event.getClick());
        } else {
            task = () -> openCategoryMenu(topHolder, clicked.getItem(event.getSlot()));
        }

        BoxAPI.api().getScheduler().runAsyncTask(task);
    }

    private void openCategoryMenu(@NotNull BoxInventoryHolder holder, @Nullable ItemStack item) {
        var boxItem = item != null ? BoxAPI.api().getItemManager().getBoxItem(item).orElse(null) : null;

        if (boxItem == null) {
            holder.finishClickProcess();
            return;
        }

        var category = findCategory(boxItem).orElse(null);

        if (category == null) {
            holder.finishClickProcess();
            return;
        }

        var menu = new CategoryMenu(category);
        int page = category.getItems().indexOf(boxItem) / menu.getIconsPerPage() + 1;
        var session = holder.getSession();

        if (holder.getMenu() instanceof CategoryMenu categoryMenu &&
                categoryMenu.getCategory() == category &&
                categoryMenu.getCurrentPage(session) == page) { // Same menu, and same page.
            holder.finishClickProcess();
            return;
        }

        menu.setCurrentPage(session, page);
        session.rememberMenu(holder.getMenu());
        MenuOpener.open(menu, holder.getSession());
        // No need to finish click process because the menu with new holder will open
    }

    private @NotNull Optional<Category> findCategory(@NotNull BoxItem item) {
        return CategoryRegistry.get().values().stream()
                .filter(category -> category.containsItem(item))
                .findFirst();
    }
}
