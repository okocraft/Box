package net.okocraft.box.api.transaction;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A record class to indicate transaction result.
 *
 * @param item   the transacted item
 * @param amount the amount of the transacted item
 */
public record TransactionResult(@NotNull BoxItem item, int amount) {

    /**
     * Creates a new {@link TransactionResult}.
     *
     * @param item   the transacted item
     * @param amount the amount of the transacted item
     * @return a new {@link TransactionResult}
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull TransactionResult create(@NotNull BoxItem item, int amount) {
        return new TransactionResult(item, amount);
    }

    /**
     * A constructor of {@link TransactionResult}.
     *
     * @param item   the transacted item
     * @param amount the amount of the transacted item
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    public TransactionResult {
        Objects.requireNonNull(item);

        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
    }

    /**
     * Gets the transacted item.
     *
     * @return the transacted item
     */
    @ApiStatus.Obsolete
    public @NotNull BoxItem getItem() {
        return this.item;
    }

    /**
     * Gets the amount of the transacted item.
     *
     * @return the amount of the transacted item
     */
    @ApiStatus.Obsolete
    public int getAmount() {
        return this.amount;
    }
}
