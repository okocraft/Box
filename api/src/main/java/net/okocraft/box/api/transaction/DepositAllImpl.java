package net.okocraft.box.api.transaction;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.stock.StockHolder;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

record DepositAllImpl(@NotNull StockHolder stockHolder,
                      @Nullable Predicate<BoxItem> filter) implements StockHolderTransaction.Deposit {

    @Override
    public @NotNull @Unmodifiable List<TransactionResult> fromInventory(@NotNull Inventory inventory, @NotNull StockEvent.Cause cause) {
        return this.fromInventory(inventory, null, BoxAPI.api().getItemManager(), cause);
    }

    @Override
    public @NotNull @Unmodifiable List<TransactionResult> fromTopInventory(@NotNull InventoryView view, @NotNull StockEvent.Cause cause) {
        return this.fromInventory(view.getTopInventory(), view, BoxAPI.api().getItemManager(), cause);
    }

    @VisibleForTesting
    @NotNull @Unmodifiable List<TransactionResult> fromInventory(@NotNull Inventory inventory, @Nullable InventoryView view, @NotNull ItemManager itemManager, @NotNull StockEvent.Cause cause) {
        Objects.requireNonNull(inventory);
        Objects.requireNonNull(cause);

        var resultMap = new Object2IntOpenHashMap<BoxItem>();
        var contents = inventory.getStorageContents();

        for (int i = 0; i < contents.length; i++) {
            var item = contents[i];

            if (item == null || item.getType().isAir()) {
                continue;
            }

            var boxItem = itemManager.getBoxItem(item).orElse(null);

            if (boxItem == null || (filter != null && !filter.test(boxItem)) || (view != null && !checkClickEvent(view, i))) {
                continue;
            }

            int itemAmount = item.getAmount();

            stockHolder.increase(boxItem, itemAmount, cause);
            contents[i] = null;

            resultMap.mergeInt(boxItem, itemAmount, Integer::sum);
        }

        if (resultMap.isEmpty()) {
            return Collections.emptyList();
        } else {
            inventory.setStorageContents(contents);
            var result = new ArrayList<TransactionResult>(resultMap.size());

            for (var entry : resultMap.object2IntEntrySet()) {
                result.add(TransactionResult.create(entry.getKey(), entry.getIntValue()));
            }

            return Collections.unmodifiableList(result);
        }
    }

    private static boolean checkClickEvent(@NotNull InventoryView view, int slot) {
        return new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, slot, ClickType.LEFT, InventoryAction.PICKUP_ALL).callEvent();
    }
}
