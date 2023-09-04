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

import java.util.Objects;

record WithdrawalImpl(@NotNull StockHolder stockHolder, @NotNull BoxItem boxItem,
                      int limit) implements StockHolderTransaction.Withdrawal {

    @Override
    public @NotNull TransactionResult toInventory(@NotNull Inventory inventory, @NotNull StockEvent.Cause cause) {
        return toInventory(inventory, null, cause);
    }

    @Override
    public @NotNull TransactionResult toTopInventory(@NotNull InventoryView view, @NotNull StockEvent.Cause cause) {
        return toInventory(view.getTopInventory(), view, cause);
    }

    private @NotNull TransactionResult toInventory(@NotNull Inventory inventory, @Nullable InventoryView view, @NotNull StockEvent.Cause cause) {
        Objects.requireNonNull(inventory);
        Objects.requireNonNull(cause);

        int maxStackSize = boxItem.getOriginal().getMaxStackSize();
        int withdrawnAmount = 0;

        var contents = inventory.getStorageContents();

        for (int i = 0; i < contents.length && withdrawnAmount < limit; i++) {
            var item = contents[i];

            if (item == null) {
                if (view != null && !checkClickEvent(view, i)) {
                    continue;
                }

                int withdrawn = stockHolder.decreaseToZero(boxItem, Math.min(limit - withdrawnAmount, maxStackSize), cause);

                if (withdrawn < 1) {
                    break;
                }

                withdrawnAmount += withdrawn;
                contents[i] = boxItem.getOriginal().asQuantity(withdrawn);
            } else if (item.isSimilar(boxItem.getOriginal())) {
                if (view != null && !checkClickEvent(view, i)) {
                    continue;
                }

                int remaining = maxStackSize - item.getAmount();

                if (0 < remaining) {
                    int withdrawn = stockHolder.decreaseToZero(boxItem, Math.min(limit - withdrawnAmount, remaining), cause);

                    if (withdrawn < 1) {
                        break;
                    }

                    withdrawnAmount += withdrawn;
                    item.setAmount(withdrawn < remaining ? item.getAmount() + withdrawn : maxStackSize);
                }
            }
        }

        if (withdrawnAmount != 0) {
            inventory.setStorageContents(contents);
        }

        return TransactionResult.create(boxItem, withdrawnAmount);
    }

    private static boolean checkClickEvent(@NotNull InventoryView view, int slot) {
        return new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, slot, ClickType.LEFT, InventoryAction.PLACE_ALL).callEvent();
    }
}
