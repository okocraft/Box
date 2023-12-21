package net.okocraft.box.api.transaction;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A utility class for transacting items between {@link StockHolder} and {@link Inventory}.
 */
public final class StockHolderTransaction {

    /**
     * Creates a {@link StockHolderTransaction}.
     *
     * @param stockHolder a {@link StockHolder} to transact
     * @return a new {@link StockHolderTransaction}
     * @throws NullPointerException if {@code stockHolder} is null
     */
    @Contract("_ -> new")
    public static @NotNull StockHolderTransaction create(@NotNull StockHolder stockHolder) {
        return new StockHolderTransaction(Objects.requireNonNull(stockHolder));
    }

    private final StockHolder stockHolder;

    private StockHolderTransaction(@NotNull StockHolder stockHolder) {
        this.stockHolder = stockHolder;
    }

    /**
     * Creates a {@link Deposit} instance that depositing the specified {@link BoxItem}.
     *
     * @param item  a {@link BoxItem} to deposit to the {@link StockHolder}
     * @param limit a maximum amount to deposit
     * @return a {@link Deposit} instance
     * @throws NullPointerException if {@code item} is null
     */
    public @NotNull Deposit deposit(@NotNull BoxItem item, int limit) {
        return new DepositImpl(stockHolder, Objects.requireNonNull(item), limit);
    }

    /**
     * Creates a {@link Deposit} instance that depositing all items that can be deposited.
     *
     * @return a {@link Deposit} instance
     */
    public @NotNull Deposit depositAll() {
        return new DepositAllImpl(stockHolder, null);
    }

    /**
     * Creates a {@link Deposit} instance that depositing filtered items that can be deposited.
     *
     * @param filter a {@link Predicate} to filter {@link BoxItem}s
     * @return a {@link Deposit} instance
     * @throws NullPointerException if {@code filter} is null
     */
    public @NotNull Deposit depositAll(@NotNull Predicate<BoxItem> filter) {
        return new DepositAllImpl(stockHolder, Objects.requireNonNull(filter));
    }

    /**
     * Creates a {@link Withdrawal} instance that withdrawing the specified {@link BoxItem}.
     *
     * @param item  a {@link BoxItem} to withdraw from the {@link StockHolder}
     * @param limit a maximum amount to withdraw
     * @return a {@link Withdrawal} instance
     * @throws NullPointerException if {@code item} is null
     */
    public @NotNull Withdrawal withdraw(@NotNull BoxItem item, int limit) {
        return new WithdrawalImpl(stockHolder, Objects.requireNonNull(item), limit);
    }

    /**
     * An interface to deposit items to {@link StockHolder}.
     */
    public interface Deposit {

        /**
         * Deposits items from {@link Inventory}.
         * <p>
         * If no items are deposited, this method returns {@link java.util.Collections#emptyList()}
         *
         * @param inventory an {@link Inventory} to pull out items
         * @param cause     a {@link StockEvent.Cause} that is passed to {@link StockHolder#increase(BoxItem, int, StockEvent.Cause)}
         * @return a list of {@link TransactionResult}
         * @throws NullPointerException if {@code inventory} or {@code cause} is null
         */
        @NotNull @Unmodifiable List<TransactionResult> fromInventory(@NotNull Inventory inventory, @NotNull StockEvent.Cause cause);

        /**
         * Deposits items from {@link InventoryView#getTopInventory()}.
         * <p>
         * If no items are deposited, this method returns {@link java.util.Collections#emptyList()}
         * <p>
         * This method calls {@link org.bukkit.event.inventory.InventoryClickEvent}s before pulling out the item.
         *
         * @param view  an {@link InventoryView} to get top {@link Inventory}
         * @param cause a {@link StockEvent.Cause} that is passed to {@link StockHolder#increase(BoxItem, int, StockEvent.Cause)}
         * @return a list of {@link TransactionResult}
         * @throws NullPointerException if {@code view} or {@code cause} is null
         */
        @NotNull @Unmodifiable List<TransactionResult> fromTopInventory(@NotNull InventoryView view, @NotNull StockEvent.Cause cause);

    }

    /**
     * An interface to withdraw items from {@link StockHolder}.
     */
    public interface Withdrawal {

        /**
         * Withdraws items to {@link Inventory}.
         *
         * @param inventory an {@link Inventory} to put items in
         * @param cause     a {@link StockEvent.Cause} that is passed to {@link StockHolder#decrease(BoxItem, int, StockEvent.Cause)}
         * @return a {@link TransactionResult}
         * @throws NullPointerException if {@code inventory} or {@code cause} is null
         */
        @NotNull TransactionResult toInventory(@NotNull Inventory inventory, @NotNull StockEvent.Cause cause);

        /**
         * Withdraws items to {@link InventoryView#getTopInventory()}.
         * <p>
         * This method calls {@link org.bukkit.event.inventory.InventoryClickEvent}s before putting the item in.
         *
         * @param view  an {@link InventoryView} to put items in
         * @param cause a {@link StockEvent.Cause} that is passed to {@link StockHolder#decrease(BoxItem, int, StockEvent.Cause)}
         * @return a {@link TransactionResult}
         * @throws NullPointerException if {@code view} or {@code cause} is null
         */
        @NotNull TransactionResult toTopInventory(@NotNull InventoryView view, @NotNull StockEvent.Cause cause);

    }
}
