package net.okocraft.box.api.transaction;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A class to indicate transaction result.
 * <p>
 * Note: This class will be a record class in Box v6.0.0
 */
@ApiStatus.NonExtendable
public class TransactionResult {

    /**
     * Creates a new {@link TransactionResult}.
     *
     * @param type the {@link TransactionResultType} whose {@link TransactionResultType#isModified()} is false
     * @return a new {@link TransactionResult}
     * @deprecated {@link TransactionResultType} will be removed in Box v6.0.0
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
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
     * @deprecated {@link TransactionResultType} will be removed in Box v6.0.0
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
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

    /**
     * Creates a new {@link TransactionResult}.
     *
     * @param item   the transacted item
     * @param amount the amount of the transacted item
     * @return a new {@link TransactionResult}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull TransactionResult create(@NotNull BoxItem item, int amount) {
        var type = 0 < amount ? TransactionResultType.NOT_SPECIFIED_TRUE : TransactionResultType.NOT_SPECIFIED_FALSE;
        return new TransactionResult(type, item, amount);
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
     * @deprecated {@link TransactionResultType} will be removed in Box v6.0.0
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
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
     * @return the amount of the transacted item, or {@code 0} if {@link TransactionResultType#isModified()} of {@link #getType()} is false
     */
    public int getAmount() {
        return amount;
    }

    public @NotNull BoxItem item() { // TODO: record
        return this.item;
    }

    public int amount() { // TODO: record
        return amount;
    }
}
