package net.okocraft.box.api.event.stock;

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
// TODO: move StockEvent classes to api.event.stockholder in version 5.0.0
public class StockEvent extends StockHolderEvent implements AsyncEvent {

    /**
     * The constructor of {@link StockEvent}.
     *
     * @param stockHolder the stockholder of the event
     */
    public StockEvent(@NotNull StockHolder stockHolder) {
        super(stockHolder);
    }
}
