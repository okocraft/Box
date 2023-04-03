package net.okocraft.box.api.transaction;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;

import static net.okocraft.box.api.transaction.TransactionResultType.DEPOSITED;
import static net.okocraft.box.api.transaction.TransactionResultType.IS_AIR;
import static net.okocraft.box.api.transaction.TransactionResultType.ITEM_NOT_REGISTERED;
import static net.okocraft.box.api.transaction.TransactionResultType.NOT_DEPOSITED;
import static net.okocraft.box.api.transaction.TransactionResultType.NOT_FOUND;
import static net.okocraft.box.api.transaction.TransactionResultType.WITHDREW;

/**
 * A class for transacting with inventory.
 * <p>
 * All methods of this class must be called in the server main thread.
 */
public final class InventoryTransaction {

    /**
     * Deposits items in player's main hand.
     *
     * @param player       the target player
     * @param depositLimit the deposit limit
     * @return the {@link TransactionResult}
     */
    public static @NotNull TransactionResult depositItemInMainHand(@NotNull Player player, int depositLimit) {
        Objects.requireNonNull(player);

        if (depositLimit < 1) {
            return TransactionResult.create(NOT_DEPOSITED);
        }

        var mainHand = player.getInventory().getItemInMainHand();

        if (mainHand.getType().isAir()) {
            return TransactionResult.create(IS_AIR);
        }

        var boxItem = BoxProvider.get().getItemManager().getBoxItem(mainHand);

        if (boxItem.isEmpty()) {
            return TransactionResult.create(ITEM_NOT_REGISTERED);
        }

        int depositAmount = Math.min(depositLimit, mainHand.getAmount());
        int afterAmount = mainHand.getAmount() - depositAmount;

        if (0 < afterAmount) {
            mainHand.setAmount(afterAmount);
            player.getInventory().setItemInMainHand(mainHand);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        return TransactionResult.create(DEPOSITED, boxItem.get(), depositAmount);
    }

    /**
     * Deposits items in an inventory.
     *
     * @param inventory the target inventory
     * @return the {@link TransactionResultList}
     */
    public static @NotNull TransactionResultList depositItemsInInventory(@NotNull Inventory inventory) {
        return depositItemsInInventory(inventory, item -> true);
    }

    /**
     * Deposits items in an inventory.
     *
     * @param inventory the target inventory
     * @param filter the {@link Predicate} deciding which items to deposit
     * @return the {@link TransactionResultList}
     */
    public static @NotNull TransactionResultList depositItemsInInventory(@NotNull Inventory inventory, @NotNull Predicate<BoxItem> filter) {
        Objects.requireNonNull(inventory);

        var result = new ArrayList<TransactionResult>();
        var contents = inventory.getStorageContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            if (item == null) {
                continue;
            }

            var boxItem = BoxProvider.get().getItemManager().getBoxItem(item);

            if (boxItem.isPresent() && filter.test(boxItem.get())) {
                result.add(TransactionResult.create(DEPOSITED, boxItem.get(), item.getAmount()));
                contents[i] = null;
            }
        }

        if (result.isEmpty()) {
            return TransactionResultList.create(NOT_FOUND);
        } else {
            inventory.setStorageContents(contents);
            return TransactionResultList.create(DEPOSITED, result);
        }
    }

    /**
     * Deposits items in an {@link InventoryView#getTopInventory()}.
     * <p>
     * This method calls {@link InventoryClickEvent} before taking the item from the slot.
     *
     * @param inventoryView the target inventory view
     * @return the {@link TransactionResultList}
     */
    public static @NotNull TransactionResultList depositItemsInTopInventory(@NotNull InventoryView inventoryView) {
        Objects.requireNonNull(inventoryView);

        var inventory = inventoryView.getTopInventory();

        var result = new ArrayList<TransactionResult>();
        var contents = inventory.getStorageContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            if (item == null) {
                continue;
            }

            var boxItem = BoxProvider.get().getItemManager().getBoxItem(item);

            if (boxItem.isPresent() && checkClickEvent(inventoryView, i)) {
                result.add(TransactionResult.create(DEPOSITED, boxItem.get(), item.getAmount()));
                contents[i] = null;
            }
        }

        if (result.isEmpty()) {
            return TransactionResultList.create(NOT_FOUND);
        } else {
            inventory.setStorageContents(contents);
            return TransactionResultList.create(DEPOSITED, result);
        }
    }

    /**
     * Deposits specified items in an inventory.
     *
     * @param inventory    the target inventory
     * @param boxItem      the item to deposit
     * @param depositLimit the deposit limit
     * @return the {@link TransactionResultList}
     */
    public static @NotNull TransactionResultList depositItem(@NotNull Inventory inventory,
                                                             @NotNull BoxItem boxItem, int depositLimit) {
        Objects.requireNonNull(inventory);
        Objects.requireNonNull(boxItem);

        if (depositLimit < 1) {
            return TransactionResultList.create(NOT_DEPOSITED);
        }

        var result = new ArrayList<TransactionResult>();
        var contents = inventory.getStorageContents();

        int deposited = 0;

        for (int i = 0; i < contents.length; i++) {
            var item = contents[i];

            if (item == null || !item.isSimilar(boxItem.getOriginal())) {
                continue;
            }

            var temp = deposited + item.getAmount();

            if (temp < depositLimit) {
                deposited = temp;

                result.add(TransactionResult.create(DEPOSITED, boxItem, item.getAmount()));
                contents[i] = null;
            } else {
                int toDeposit = depositLimit - deposited;

                result.add(TransactionResult.create(DEPOSITED, boxItem, toDeposit));

                var after = item.getAmount() - toDeposit;

                if (0 < after) {
                    item.setAmount(after);
                } else {
                    contents[i] = null;
                }

                break;
            }
        }

        if (result.isEmpty()) {
            return TransactionResultList.create(NOT_FOUND);
        } else {
            inventory.setStorageContents(contents);
            return TransactionResultList.create(DEPOSITED, result);
        }
    }

    /**
     * Withdraws items to an inventory.
     *
     * @param inventory the target inventory
     * @param boxItem   the item to withdraw
     * @param amount    the amount of item
     * @return the {@link TransactionResult}
     */
    public static @NotNull TransactionResult withdraw(@NotNull Inventory inventory,
                                                      @NotNull BoxItem boxItem, int amount) {
        Objects.requireNonNull(inventory);
        Objects.requireNonNull(boxItem);

        return withdraw(null, inventory, boxItem, amount);
    }

    /**
     * Withdraws items to an {@link InventoryView#getTopInventory()}.
     * <p>
     * This method calls {@link InventoryClickEvent} before storing the item to the slot.
     *
     * @param inventoryView the target inventory view
     * @param boxItem       the item to withdraw
     * @param amount        the amount of item
     * @return the {@link TransactionResult}
     */
    public static @NotNull TransactionResult withdraw(@NotNull InventoryView inventoryView,
                                                      @NotNull BoxItem boxItem, int amount) {
        Objects.requireNonNull(inventoryView);
        Objects.requireNonNull(boxItem);

        return withdraw(inventoryView, inventoryView.getTopInventory(), boxItem, amount);
    }

    private static @NotNull TransactionResult withdraw(@Nullable InventoryView view, @NotNull Inventory inventory,
                                                       @NotNull BoxItem boxItem, int amount) {
        var toStore = amount;
        var maxStackSize = boxItem.getOriginal().getMaxStackSize();

        var contents = inventory.getStorageContents();

        for (int i = 0; i < contents.length && 0 < toStore; i++) {
            var item = contents[i];

            if (item == null) {
                if (view != null && !checkClickEvent(view, i)) {
                    continue;
                }

                var cloned = boxItem.getClonedItem();

                if (toStore < maxStackSize) {
                    cloned.setAmount(toStore);
                    toStore = 0;
                } else {
                    cloned.setAmount(maxStackSize);
                    toStore -= maxStackSize;
                }

                contents[i] = cloned;
            } else if (item.isSimilar(boxItem.getOriginal())) {
                if (view != null && !checkClickEvent(view, i)) {
                    continue;
                }

                var remaining = maxStackSize - item.getAmount();

                if (0 < remaining) {
                    if (toStore < remaining) {
                        item.setAmount(item.getAmount() + toStore);
                        toStore = 0;
                    } else {
                        item.setAmount(maxStackSize);
                        toStore -= remaining;
                    }
                }
            }
        }

        inventory.setStorageContents(contents);

        if (toStore == 0) {
            return TransactionResult.create(WITHDREW, boxItem, amount);
        }

        if (toStore == amount) {
            return TransactionResult.create(TransactionResultType.INVENTORY_IS_FULL);
        } else {
            return TransactionResult.create(TransactionResultType.WITHDREW_PARTIAL, boxItem, amount - toStore);
        }
    }

    private static boolean checkClickEvent(@NotNull InventoryView view, int slot) {
        return new InventoryClickEvent(
                view, InventoryType.SlotType.CONTAINER, slot,
                ClickType.LEFT, InventoryAction.PLACE_ALL).callEvent();
    }
}
