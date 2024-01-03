package net.okocraft.box.api.transaction;

import org.jetbrains.annotations.ApiStatus;

/**
 * An enum of transaction result types.
 *
 * @deprecated no replacements
 */
@Deprecated(since = "5.5.0", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
public enum TransactionResultType {
    /**
     * A result type when the item in the player's main hand is air.
     * <p>
     * {@link #isModified()} of this type is {@code false}.
     */
    IS_AIR(false),

    /**
     * A result type when the item in the player's main hand is not registered.
     * <p>
     * {@link #isModified()} of this type is {@code false}.
     */
    ITEM_NOT_REGISTERED(false),

    /**
     * A result type when the target inventory is full.
     * <p>
     * {@link #isModified()} of this type is {@code false}.
     */
    INVENTORY_IS_FULL(false),

    /**
     * A result type when the specified item is not found in the inventory.
     * <p>
     * {@link #isModified()} of this type is {@code false}.
     */
    NOT_FOUND(false),

    /**
     * A result type when no items are deposited.
     * <p>
     * {@link #isModified()} of this type is {@code false}.
     */
    NOT_DEPOSITED(false),

    /**
     * A result type when items are successfully deposited.
     * <p>
     * {@link #isModified()} of this type is {@code true}.
     */
    DEPOSITED(true),

    /**
     * A result type when items are successfully withdrawn.
     * <p>
     * {@link #isModified()} of this type is {@code true}.
     */
    WITHDREW(true),

    /**
     * A result type when some items are withdrawn.
     * <p>
     * {@link #isModified()} of this type is {@code true}.
     */
    WITHDREW_PARTIAL(true),

    /**
     * A result type for compatibility
     */
    @ApiStatus.Internal
    NOT_SPECIFIED_TRUE(true),

    /**
     * A result type for compatibility
     */
    @ApiStatus.Internal
    NOT_SPECIFIED_FALSE(false);

    private final boolean modified;

    TransactionResultType(boolean modified) {
        this.modified = modified;
    }

    /**
     * Check if the transaction has resulted in any changes.
     *
     * @return whether any changes were made as a result of the transaction
     */
    public boolean isModified() {
        return modified;
    }
}