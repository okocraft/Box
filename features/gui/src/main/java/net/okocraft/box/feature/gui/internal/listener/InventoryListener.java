package net.okocraft.box.feature.gui.internal.listener;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.gui.api.event.stock.GuiCauses;
import net.okocraft.box.feature.gui.api.session.MenuHistoryHolder;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.gui.internal.menu.CategoryMenu;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class InventoryListener implements Listener {

    private static final long CLICK_COOLDOWN = TimeUnit.MILLISECONDS.toNanos(150);

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(@NotNull InventoryClickEvent event) {
        BoxInventoryHolder topHolder = BoxInventoryHolder.getFromInventory(event.getView().getTopInventory());

        if (topHolder == null) {
            return;
        }

        event.setCancelled(true);

        Inventory clicked = event.getClickedInventory();

        if (clicked == null || System.nanoTime() - topHolder.getLastClickTime() < CLICK_COOLDOWN) {
            return;
        }

        topHolder.updateLastClickTime();

        if (!topHolder.tryStartClickProcess()) {
            return;
        }

        if (BoxInventoryHolder.isBoxMenu(clicked)) {
            BoxAPI.api().getScheduler().runAsyncTask(() -> topHolder.processClick(event.getSlot(), event.getClick()));
        } else {
            try {
                this.onClickPlayerInventory(topHolder, clicked, event.getSlot(), event.getClick());
            } finally {
                topHolder.finishClickProcess();
            }
        }
    }

    private void onClickPlayerInventory(@NotNull BoxInventoryHolder holder, @NotNull Inventory inventory, int slot, @NotNull ClickType clickType) {
        ItemStack item = inventory.getItem(slot);
        BoxItem boxItem = item != null ? BoxAPI.api().getItemManager().getBoxItem(item).orElse(null) : null;

        if (boxItem == null) {
            return;
        }

        if (clickType.isLeftClick() && clickType.isShiftClick()) {
            this.depositClickedItem(holder, inventory, slot, item, boxItem);
        } else {
            this.openCategoryMenu(holder, boxItem);
        }
    }

    private void depositClickedItem(@NotNull BoxInventoryHolder holder, @NotNull Inventory inventory, int slot, @NotNull ItemStack item, @NotNull BoxItem boxItem) {
        Player viewer = holder.getSession().getViewer();
        int amount = item.getAmount();

        StockHolder stockHolder = holder.getSession().getSourceStockHolder();
        GuiCauses.Deposit cause = new GuiCauses.Deposit(viewer);

        if (amount == 1) {
            inventory.setItem(slot, null);
            stockHolder.increase(boxItem, 1, cause);
        } else {
            item.setAmount(1);
            stockHolder.increase(boxItem, amount - 1, cause);
        }

        viewer.playSound(viewer, Sound.ENTITY_ITEM_PICKUP, 100f, 1.0f);

        if (holder.getMenu() instanceof CategoryMenu categoryMenu && categoryMenu.getCategory().containsItem(boxItem)) {
            holder.renderButtons();
        }
    }

    private void openCategoryMenu(@NotNull BoxInventoryHolder holder, @NotNull BoxItem boxItem) {
        if (holder.getMenu() instanceof CategoryMenu categoryMenu && categoryMenu.getCategory().containsItem(boxItem)) {
            return;
        }

        Category category = this.findCategory(boxItem).orElse(null);

        if (category == null) {
            return;
        }

        PlayerSession session = holder.getSession();
        CategoryMenu menu = new CategoryMenu(category);
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
