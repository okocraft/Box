package net.okocraft.box.api.event.stockholder.stock;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link StockEvent} called when the stock is increased.
 */
public class StockIncreaseEvent extends StockEvent {

    private final BoxItem item;
    private final int increments;
    private final int amount;

    /**
     * The constructor of {@link StockEvent}.
     *
     * @param stockHolder   the stockholder of the event
     * @param item          the item of the stock
     * @param increments    the amount of increased
     * @param currentAmount the current amount of the stock
     */
    public StockIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item,
                              int increments, int currentAmount) {
        super(stockHolder);
        this.item = Objects.requireNonNull(item);
        this.increments = increments;
        this.amount = currentAmount;
    }

    /**
     * Gets the item of the stock
     *
     * @return the item of the stock
     */
    public @NotNull BoxItem getItem() {
        return item;
    }

    /**
     * Gets the amount of increased.
     *
     * @return the amount of increased
     */
    public int getIncrements() {
        return increments;
    }

    /**
     * Gets the current amount of the stock.
     *
     * @return the current amount of the stock
     */
    public int getAmount() {
        return amount;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "StockIncreaseEvent{" +
                "stockholderUuid=" + getStockHolder().getUUID() +
                ", stockHolderName=" + getStockHolder().getName() +
                ", stockHolderClass=" + getStockHolder().getClass().getSimpleName() +
                ", item=" + item +
                ", increments=" + increments +
                ", amount=" + amount +
                '}';
    }

    @Override
    public String toString() {
        return "StockIncreaseEvent{" +
                "stockholder=" + getStockHolder() +
                ", item=" + item +
                ", increments=" + increments +
                ", amount=" + amount +
                '}';
    }
}
