package net.okocraft.box.api.transaction;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A class to indicate transaction result.
 */
public class TransactionResult {

    /**
     * Creates a new {@link TransactionResult}.
     *
     * @param type the {@link TransactionResultType} whose {@link TransactionResultType#isModified()} is false
     * @return a new {@link TransactionResult}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull TransactionResult create(@NotNull TransactionResultType type) {
        Objects.requireNonNull(type);

        if (!type.isModified()) {
            return new TransactionResult(type, null, 0);
        } else {
            throw new IllegalArgumentException(
                    "A type whose TransactionResultType#isModified is true cannot be used for this method"
            );
        }
    }

    /**
     * Creates a new {@link TransactionResult}.
     *
     * @param type   the {@link TransactionResultType} whose {@link TransactionResultType#isModified()} is true
     * @param item   the transacted item
     * @param amount the amount of the transacted item
     * @return a new {@link TransactionResult}
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull TransactionResult create(@NotNull TransactionResultType type, @NotNull BoxItem item, int amount) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(item);

        if (type.isModified()) {
            return new TransactionResult(type, item, amount);
        } else {
            throw new IllegalArgumentException(
                    "A type whose TransactionResultType#isModified is false cannot be used for this method"
            );
        }
    }

    private final TransactionResultType type;
    private final BoxItem item;
    private final int amount;

    private TransactionResult(@NotNull TransactionResultType type, @Nullable BoxItem item, int amount) {
        this.type = type;
        this.item = type.isModified() ? Objects.requireNonNull(item) : null;
        this.amount = amount;
    }

    /**
     * Gets the transaction result type.
     *
     * @return the {@link TransactionResultType}
     */
    public @NotNull TransactionResultType getType() {
        return type;
    }

    /**
     * Gets the transacted item.
     *
     * @return the transacted item
     * @throws IllegalStateException the {@link TransactionResultType#isModified()} of {@link #getType()} is false
     */
    public @NotNull BoxItem getItem() {
        if (item != null) {
            return item;
        } else {
            throw new IllegalStateException(
                    "Could not get the item because TransactionResultType#isModified is false"
            );
        }
    }

    /**
     * Gets the amount of the transacted item.
     *
     * @return the amount of the transacted item
     * @throws IllegalStateException the {@link TransactionResultType#isModified()} of {@link #getType()} is false
     */
    public int getAmount() {
        if (type.isModified()) {
            return amount;
        } else {
            throw new IllegalStateException(
                    "Could not get the item because TransactionResultType#isModified is false"
            );
        }
    }
}
