package net.okocraft.box.api.event.stockholder.stock;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.ApiStatus;
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
     * @deprecated use {@link #StockSetEvent(StockHolder, BoxItem, int, int, Cause)}
     */
    @Deprecated(forRemoval = true, since = "5.2.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.3.0")
    public StockSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, int previousAmount) {
        this(stockHolder, item, amount, previousAmount, Cause.API);
    }

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
