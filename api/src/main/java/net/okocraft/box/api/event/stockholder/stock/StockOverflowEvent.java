package net.okocraft.box.api.event.stockholder.stock;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link StockEvent} called when the amount of stock exceeds {@link Integer#MAX_VALUE}.
 */
public class StockOverflowEvent extends StockIncreaseEvent {

    private final int excess;

    /**
     * The constructor of {@link StockOverflowEvent}.
     *
     * @param stockHolder the {@link StockHolder} of this event
     * @param item        the item that has overflowed the amount of stock
     * @param increments  the amount of increased (excess is not included)
     * @param excess      the amount exceeding {@link Integer#MAX_VALUE}
     * @param cause       the cause that indicates why this event was called
     */
    public StockOverflowEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item,
                              int increments, int excess, @NotNull Cause cause) {
        super(stockHolder, item, increments, Integer.MAX_VALUE, cause);
        this.excess = excess;
    }

    /**
     * Gets the amount exceeding {@link Integer#MAX_VALUE}.
     *
     * @return the amount exceeding {@link Integer#MAX_VALUE}
     */
    public int getExcess() {
        return this.excess;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "StockOverflowEvent{" +
                "stockholderUuid=" + this.getStockHolder().getUUID() +
                ", stockHolderName=" + this.getStockHolder().getName() +
                ", stockHolderClass=" + this.getStockHolder().getClass().getSimpleName() +
                ", item=" + this.getItem().getPlainName() +
                ", increments=" + this.getIncrements() +
                ", excess=" + this.getExcess() +
                ", amount=" + this.getAmount() +
                ", cause=" + this.getCause() +
                '}';
    }

    @Override
    public String toString() {
        return "StockOverflowEvent{" +
                "stockholder=" + this.getStockHolder() +
                ", item=" + this.getItem() +
                ", increments=" + this.getIncrements() +
                ", excess=" + this.getExcess() +
                ", amount=" + this.getAmount() +
                ", cause=" + this.getCause() +
                '}';
    }
}
