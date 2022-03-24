package net.okocraft.box.api.event.stockholder.stock;

import net.okocraft.box.api.event.AsyncEvent;
import net.okocraft.box.api.event.stockholder.StockHolderEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link StockHolderEvent} called when the stock of the {@link StockHolder} is modified.
 * <p>
 * You need to be very careful not to call methods that cause changes to a {@link StockHolder} in the event,
 * as this may result in an infinite loop.
 */
public class StockEvent extends StockHolderEvent implements AsyncEvent {

    private final BoxItem item;
    private final int amount;

    /**
     * The constructor of {@link StockEvent}.
     *
     * @param stockHolder the stockholder of the event
     * @param item        the item of the stock
     * @param amount      the current amount of the stock
     */
    public StockEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount) {
        super(stockHolder);
        this.item = Objects.requireNonNull(item);
        this.amount = amount;
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
     * Gets the current amount of the stock.
     *
     * @return the current amount of the stock
     */
    public int getAmount() {
        return amount;
    }
}
