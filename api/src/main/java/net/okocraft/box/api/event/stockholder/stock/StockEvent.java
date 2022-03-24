package net.okocraft.box.api.event.stockholder.stock;

import net.okocraft.box.api.event.AsyncEvent;
import net.okocraft.box.api.event.stockholder.StockHolderEvent;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link StockHolderEvent} called when the stock of the {@link StockHolder} is modified.
 * <p>
 * You need to be very careful not to call methods that cause changes to a {@link StockHolder} in the event,
 * as this may result in an infinite loop.
 */
public class StockEvent extends StockHolderEvent implements AsyncEvent {

    private final int amount;

    /**
     * The constructor of {@link StockEvent}.
     *
     * @param stockHolder the stockholder of the event
     */
    public StockEvent(@NotNull StockHolder stockHolder, int amount) {
        super(stockHolder);
        this.amount = amount;
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
