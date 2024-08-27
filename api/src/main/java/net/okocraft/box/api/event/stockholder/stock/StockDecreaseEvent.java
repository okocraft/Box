package net.okocraft.box.api.event.stockholder.stock;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link StockEvent} called when the stock is decreased.
 */
public class StockDecreaseEvent extends StockEvent {

    private final int decrements;

    /**
     * The constructor of {@link StockDecreaseEvent}.
     *
     * @param stockHolder   the stockholder of the event
     * @param item          the item of the stock
     * @param decrements    the amount of decreased
     * @param currentAmount the current amount of the stock
     * @param cause         the cause that indicates why this event was called
     */
    public StockDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item,
                              int decrements, int currentAmount, @NotNull Cause cause) {
        super(stockHolder, item, currentAmount, cause);
        this.decrements = decrements;
    }

    /**
     * Gets the amount of decreased.
     *
     * @return the amount of decreased
     */
    public int getDecrements() {
        return this.decrements;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "StockDecreaseEvent{" +
            "stockholderUuid=" + this.getStockHolder().getUUID() +
            ", stockHolderName=" + this.getStockHolder().getName() +
            ", stockHolderClass=" + this.getStockHolder().getClass().getSimpleName() +
            ", item=" + this.getItem().getPlainName() +
            ", decrements=" + this.decrements +
            ", amount=" + this.getAmount() +
            ", cause=" + this.getCause() +
            '}';
    }

    @Override
    public String toString() {
        return "StockDecreaseEvent{" +
            "stockholder=" + this.getStockHolder() +
            ", item=" + this.getItem() +
            ", decrements=" + this.decrements +
            ", amount=" + this.getAmount() +
            ", cause=" + this.getCause() +
            '}';
    }
}
