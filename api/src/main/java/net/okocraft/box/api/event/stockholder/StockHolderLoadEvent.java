package net.okocraft.box.api.event.stockholder;

import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link StockHolderEvent} called when the stockholder is loaded.
 */
public class StockHolderLoadEvent extends StockHolderEvent {

    /**
     * The constructor of {@link StockHolderLoadEvent}.
     *
     * @param stockHolder the loaded stockholder
     */
    public StockHolderLoadEvent(@NotNull StockHolder stockHolder) {
        super(stockHolder);
    }
}
