package net.okocraft.box.api.event.stock;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link StockEvent} called when the stock is decreased.
 */
public class StockDecreaseEvent extends StockEvent {

    private final BoxItem item;
    private final int decrements;
    private final int amount;

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
        super(stockHolder);
        this.item = Objects.requireNonNull(item);
        this.decrements = decrements;
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
     * Gets the amount of decreased.
     *
     * @return the amount of decreased
     */
    public int getDecrements() {
        return decrements;
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
        return "StockDecreaseEvent{" +
                "stockholderUuid=" + getStockHolder().getUUID() +
                ", stockHolderName=" + getStockHolder().getName() +
                ", stockHolderClass=" + getStockHolder().getClass().getSimpleName() +
                ", item=" + item +
                ", decrements=" + decrements +
                ", amount=" + amount +
                '}';
    }

    @Override
    public String toString() {
        return "StockDecreaseEvent{" +
                "stockholder=" + getStockHolder() +
                ", item=" + item +
                ", decrements=" + decrements +
                ", amount=" + amount +
                '}';
    }
}
