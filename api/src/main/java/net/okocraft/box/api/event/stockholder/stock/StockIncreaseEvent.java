package net.okocraft.box.api.event.stockholder.stock;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link StockEvent} called when the stock is increased.
 */
public class StockIncreaseEvent extends StockEvent {

    private final int increments;

    /**
     * The constructor of {@link StockIncreaseEvent}.
     *
     * @param stockHolder   the stockholder of the event
     * @param item          the item of the stock
     * @param increments    the amount of increased
     * @param currentAmount the current amount of the stock
     * @deprecated use {@link #StockIncreaseEvent(StockHolder, BoxItem, int, int, Cause)}
     */
    @Deprecated(forRemoval = true, since = "5.2.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.3.0")
    public StockIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item,
                              int increments, int currentAmount) {
        this(stockHolder, item, increments, currentAmount, Cause.API);
    }

    /**
     * The constructor of {@link StockIncreaseEvent}.
     *
     * @param stockHolder   the stockholder of the event
     * @param item          the item of the stock
     * @param increments    the amount of increased
     * @param currentAmount the current amount of the stock
     * @param cause         the cause that indicates why this event was called
     */
    public StockIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item,
                              int increments, int currentAmount, @NotNull Cause cause) {
        super(stockHolder, item, currentAmount, cause);
        this.increments = increments;
    }

    /**
     * Gets the amount of increased.
     *
     * @return the amount of increased
     */
    public int getIncrements() {
        return increments;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "StockIncreaseEvent{" +
                "stockholderUuid=" + getStockHolder().getUUID() +
                ", stockHolderName=" + getStockHolder().getName() +
                ", stockHolderClass=" + getStockHolder().getClass().getSimpleName() +
                ", item=" + getItem() +
                ", increments=" + increments +
                ", amount=" + getAmount() +
                ", cause=" + getCause() +
                '}';
    }

    @Override
    public String toString() {
        return "StockIncreaseEvent{" +
                "stockholder=" + getStockHolder() +
                ", item=" + getItem() +
                ", increments=" + increments +
                ", amount=" + getAmount() +
                ", cause=" + getCause() +
                '}';
    }
}
