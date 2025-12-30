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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

record DepositImpl(@NotNull StockHolder stockHolder, @NotNull BoxItem boxItem,
                   int limit) implements StockHolderTransaction.Deposit {

    @Override
    public @NotNull @Unmodifiable List<TransactionResult> fromInventory(@NotNull Inventory inventory, @NotNull StockEvent.Cause cause) {
        return this.fromInventory(inventory, null, cause);
    }

    @Override
    public @NotNull @Unmodifiable List<TransactionResult> fromTopInventory(@NotNull InventoryView view, @NotNull StockEvent.Cause cause) {
        return this.fromInventory(view.getTopInventory(), view, cause);
    }

    private @NotNull List<TransactionResult> fromInventory(@NotNull Inventory inventory, @Nullable InventoryView view, @NotNull StockEvent.Cause cause) {
        Objects.requireNonNull(inventory);
        Objects.requireNonNull(cause);

        ItemStack[] contents = inventory.getStorageContents();

        int depositedAmount = 0;

        for (int i = 0; i < contents.length && depositedAmount < this.limit; i++) {
            ItemStack item = contents[i];

            if (item == null || !this.boxItem.getOriginal().isSimilar(item) || (view != null && !checkClickEvent(view, i))) {
                continue;
            }

            int remaining = this.limit - depositedAmount;
            int itemAmount = item.getAmount();

            if (itemAmount <= remaining) {
                this.stockHolder.increase(this.boxItem, itemAmount, cause);
                depositedAmount += itemAmount;
                contents[i] = null;
            } else {
                this.stockHolder.increase(this.boxItem, remaining, cause);
                depositedAmount += remaining;
                contents[i] = item.asQuantity(itemAmount - remaining);
            }
        }

        if (depositedAmount != 0) {
            inventory.setStorageContents(contents);
            return List.of(TransactionResult.create(this.boxItem, depositedAmount));
        } else {
            return Collections.emptyList();
        }
    }

    private static boolean checkClickEvent(@NotNull InventoryView view, int slot) {
        return new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, slot, ClickType.LEFT, InventoryAction.PICKUP_ALL).callEvent();
    }
}
