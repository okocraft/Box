package net.okocraft.box.feature.gui.internal.listener;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.gui.api.event.stock.GuiCauses;
import net.okocraft.box.feature.gui.api.session.MenuHistoryHolder;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.gui.internal.menu.CategoryMenu;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

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

        if (clicked.getHolder() instanceof BoxInventoryHolder holder) {
            BoxAPI.api().getScheduler().runAsyncTask(() -> holder.processClick(event.getSlot(), event.getClick()));
        } else {
            try {
                this.onClickPlayerInventory(topHolder, clicked, event.getSlot(), event.getClick());
            } finally {
                topHolder.finishClickProcess();
            }
        }
    }

    private void onClickPlayerInventory(@NotNull BoxInventoryHolder holder, @NotNull Inventory inventory, int slot, @NotNull ClickType clickType) {
        var item = inventory.getItem(slot);
        var boxItem = item != null ? BoxAPI.api().getItemManager().getBoxItem(item).orElse(null) : null;

        if (boxItem == null) {
            return;
        }

        if (clickType.isLeftClick() && clickType.isShiftClick()) {
            this.depositClickedItem(holder, inventory, slot, item.getAmount(), boxItem);
        } else {
            this.openCategoryMenu(holder, boxItem);
        }
    }

    private void depositClickedItem(@NotNull BoxInventoryHolder holder, @NotNull Inventory inventory, int slot, int amount, @NotNull BoxItem boxItem) {
        var viewer = holder.getSession().getViewer();
        holder.getSession().getSourceStockHolder().increase(boxItem, amount, new GuiCauses.Deposit(viewer));

        inventory.setItem(slot, null);
        viewer.playSound(viewer, Sound.ENTITY_ITEM_PICKUP, 100f, 1.0f);

        if (holder.getMenu() instanceof CategoryMenu categoryMenu && categoryMenu.getCategory().containsItem(boxItem)) {
            holder.renderButtons();
        }
    }

    private void openCategoryMenu(@NotNull BoxInventoryHolder holder, @NotNull BoxItem boxItem) {
        if (holder.getMenu() instanceof CategoryMenu categoryMenu && categoryMenu.getCategory().containsItem(boxItem)) {
            return;
        }

        var category = findCategory(boxItem).orElse(null);

        if (category == null) {
            return;
        }

        var session = holder.getSession();
        var menu = new CategoryMenu(category);
        int page = category.getItems().indexOf(boxItem) / menu.getIconsPerPage() + 1;

        menu.setCurrentPage(session, page);
        MenuHistoryHolder.getFromSession(session).rememberMenu(holder.getMenu());
        MenuOpener.open(menu, holder.getSession());
    }

    private @NotNull Optional<Category> findCategory(@NotNull BoxItem item) {
        return CategoryRegistry.get().values().stream()
                .filter(category -> category.containsItem(item))
                .findFirst();
    }
}
