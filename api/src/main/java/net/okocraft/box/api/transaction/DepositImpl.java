package net.okocraft.box.api.transaction;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

record DepositImpl(@NotNull StockHolder stockHolder, @NotNull BoxItem boxItem,
                   int limit) implements StockHolderTransaction.Deposit {

    @Override
    public @NotNull @Unmodifiable List<TransactionResult> fromInventory(@NotNull Inventory inventory, @NotNull StockEvent.Cause cause) {
        return fromInventory(inventory, null, cause);
    }

    @Override
    public @NotNull @Unmodifiable List<TransactionResult> fromTopInventory(@NotNull InventoryView view, @NotNull StockEvent.Cause cause) {
        return fromInventory(view.getTopInventory(), view, cause);
    }

    private @NotNull List<TransactionResult> fromInventory(@NotNull Inventory inventory, @Nullable InventoryView view, @NotNull StockEvent.Cause cause) {
        Objects.requireNonNull(inventory);
        Objects.requireNonNull(cause);

        if (limit < 1) {
            return Collections.emptyList();
        }

        var result = new ArrayList<TransactionResult>();
        var contents = inventory.getStorageContents();

        int depositedAmount = 0;

        for (int i = 0; i < contents.length && depositedAmount < limit; i++) {
            var item = contents[i];

            if (item == null || !boxItem.getOriginal().isSimilar(item) || (view != null && !checkClickEvent(view, i))) {
                continue;
            }

            int remaining = limit - depositedAmount;
            int itemAmount = item.getAmount();

            if (itemAmount <= remaining) {
                depositedAmount += itemAmount;

                stockHolder.increase(boxItem, itemAmount, cause);
                contents[i] = null;

                result.add(TransactionResult.create(boxItem, itemAmount));
            } else {
                depositedAmount += remaining;

                stockHolder.increase(boxItem, remaining, cause);
                contents[i] = item.asQuantity(itemAmount - remaining);

                result.add(TransactionResult.create(boxItem, remaining));
            }
        }

        if (result.isEmpty()) {
            return Collections.emptyList();
        } else {
            inventory.setStorageContents(contents);
            return Collections.unmodifiableList(result);
        }
    }

    private static boolean checkClickEvent(@NotNull InventoryView view, int slot) {
        return new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, slot, ClickType.LEFT, InventoryAction.PICKUP_ALL).callEvent();
    }
}
