package net.okocraft.box.api.event.stockholder.stock;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link StockEvent} called when the amount of the stock is set.
 */
public class StockSetEvent extends StockEvent {

    private final int previousAmount;

    /**
     * The constructor of {@link StockSetEvent}.
     *
     * @param stockHolder    the stockholder of the event
     * @param item           the item of the stock
     * @param amount         the current amount of the stock
     * @param previousAmount the amount of stock before set
     * @param cause          the cause that indicates why this event was called
     */
    public StockSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, int previousAmount, @NotNull Cause cause) {
        super(stockHolder, item, amount, cause);
        this.previousAmount = previousAmount;
    }

    /**
     * Gets the amount of stock before set.
     *
     * @return the amount of stock before set
     */
    public int getPreviousAmount() {
        return previousAmount;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "StockSetEvent{" +
                "stockholderUuid=" + getStockHolder().getUUID() +
                ", stockHolderName=" + getStockHolder().getName() +
                ", stockHolderClass=" + getStockHolder().getClass().getSimpleName() +
                ", item=" + getItem() +
                ", previousAmount=" + previousAmount +
                ", amount=" + getAmount() +
                ", cause=" + getCause() +
                '}';
    }

    @Override
    public String toString() {
        return "StockSetEvent{" +
                "stockholder=" + getStockHolder() +
                ", item=" + getItem() +
                ", previousAmount=" + previousAmount +
                ", amount=" + getAmount() +
                ", cause=" + getCause() +
                '}';
    }
}
