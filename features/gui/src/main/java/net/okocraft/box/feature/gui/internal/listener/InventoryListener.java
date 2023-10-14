package net.okocraft.box.feature.gui.internal.listener;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.gui.api.menu.paginate.PaginatedMenu;
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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryListener implements Listener {

    private final Set<UUID> clickProcessing = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastClickTime = new ConcurrentHashMap<>();

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

        var uuid = event.getWhoClicked().getUniqueId();

        if (clickProcessing.contains(uuid)) {
            return;
        }

        var lastClickTime = this.lastClickTime.get(uuid);

        if (lastClickTime != null && System.currentTimeMillis() - lastClickTime < 150) {
            return;
        }

        this.clickProcessing.add(uuid);
        this.lastClickTime.put(uuid, System.currentTimeMillis());

        Runnable task;

        if (clicked.getHolder() instanceof BoxInventoryHolder holder) {
            task = () -> holder.processClick(event.getSlot(), event.getClick(), clickProcessing::remove);
        } else {
            task = () -> openCategoryMenu(topHolder, clicked.getItem(event.getSlot()));
        }

        BoxProvider.get().getScheduler().runAsyncTask(task);
    }

    private void openCategoryMenu(@NotNull BoxInventoryHolder holder, @Nullable ItemStack item) {
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

        if (holder.getMenu() instanceof CategoryMenu categoryMenu &&
                categoryMenu.getCategory() == category &&
                PaginatedMenu.getCurrentPage(holder.getSession()) == page) { // Same menu, and same page.
            return;
        }

        holder.getSession().putData(PaginatedMenu.CURRENT_PAGE_KEY, page);
        MenuOpener.open(menu, holder.getSession(), clickProcessing::remove);
    }

    private @NotNull Optional<Category> findCategory(@NotNull BoxItem item) {
        return CategoryRegistry.get().values().stream()
                .filter(category -> category.containsItem(item))
                .findFirst();
    }
}
