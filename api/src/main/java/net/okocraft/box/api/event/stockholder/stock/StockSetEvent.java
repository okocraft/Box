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
        return this.previousAmount;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "StockSetEvent{" +
                "stockholderUuid=" + this.getStockHolder().getUUID() +
                ", stockHolderName=" + this.getStockHolder().getName() +
                ", stockHolderClass=" + this.getStockHolder().getClass().getSimpleName() +
                ", item=" + this.getItem().getPlainName() +
                ", previousAmount=" + this.previousAmount +
                ", amount=" + this.getAmount() +
                ", cause=" + this.getCause() +
                '}';
    }

    @Override
    public String toString() {
        return "StockSetEvent{" +
                "stockholder=" + this.getStockHolder() +
                ", item=" + this.getItem() +
                ", previousAmount=" + this.previousAmount +
                ", amount=" + this.getAmount() +
                ", cause=" + this.getCause() +
                '}';
    }
}
