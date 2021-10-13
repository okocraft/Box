package net.okocraft.box.api.event.stockholder;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.stock.StockSaveEvent;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link StockHolderEvent} called when the stockholder is saved.
 */
public class StockHolderSaveEvent extends StockHolderEvent {

    /**
     * The constructor of {@link StockHolderSaveEvent}.
     *
     * @param stockHolder the saved stockholder
     */
    public StockHolderSaveEvent(@NotNull StockHolder stockHolder) {
        super(stockHolder);
        BoxProvider.get().getEventBus().callEvent(new StockSaveEvent(stockHolder)); // for compatibility
    }

    @Override
    public String toString() {
        return "StockHolderSaveEvent{" +
                "stockholder=" + getStockHolder() +
                "}";
    }
}