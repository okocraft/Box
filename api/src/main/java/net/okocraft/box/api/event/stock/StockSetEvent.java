package net.okocraft.box.api.event.stock;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link StockEvent} called when the amount of the stock is set.
 */
public class StockSetEvent extends StockEvent {

    private final BoxItem item;
    private final int amount;

    /**
     * The constructor of {@link StockEvent}.
     *
     * @param stockHolder the stockholder of the event
     * @param item        the item of the stock
     * @param amount      the current amount of the stock
     */
    public StockSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount) {
        super(stockHolder);
        this.item = item;
        this.amount = amount;
    }

    /**
     * Gets the item of the stock.
     *
     * @return the item of the stock
     */
    public @NotNull BoxItem getItem() {
        return item;
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
    public String toString() {
        return "StockSetEvent{" +
                "stockholder=" + getStockHolder() +
                ", item=" + item +
                ", amount=" + amount +
                '}';
    }
}
