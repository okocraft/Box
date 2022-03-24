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
     * The constructor of {@link StockEvent}.
     *
     * @param stockHolder   the stockholder of the event
     * @param item          the item of the stock
     * @param decrements    the amount of decreased
     * @param currentAmount the current amount of the stock
     */
    public StockDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item,
                              int decrements, int currentAmount) {
        super(stockHolder, item, currentAmount);
        this.decrements = decrements;
    }

    /**
     * Gets the amount of decreased.
     *
     * @return the amount of decreased
     */
    public int getDecrements() {
        return decrements;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "StockDecreaseEvent{" +
                "stockholderUuid=" + getStockHolder().getUUID() +
                ", stockHolderName=" + getStockHolder().getName() +
                ", stockHolderClass=" + getStockHolder().getClass().getSimpleName() +
                ", item=" + getItem() +
                ", decrements=" + decrements +
                ", amount=" + getAmount() +
                '}';
    }

    @Override
    public String toString() {
        return "StockDecreaseEvent{" +
                "stockholder=" + getStockHolder() +
                ", item=" + getItem() +
                ", decrements=" + decrements +
                ", amount=" + getAmount() +
                '}';
    }
}
