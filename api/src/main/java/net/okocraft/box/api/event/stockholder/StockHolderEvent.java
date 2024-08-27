package net.okocraft.box.api.event.stockholder;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class that represents a {@link StockHolder} related event.
 */
public class StockHolderEvent extends BoxEvent {

    private final StockHolder stockHolder;

    /**
     * The constructor of {@link StockHolderEvent}.
     *
     * @param stockHolder the stockholder of the event
     */
    public StockHolderEvent(@NotNull StockHolder stockHolder) {
        this.stockHolder = Objects.requireNonNull(stockHolder);
    }

    /**
     * Gets the stockholder.
     *
     * @return the stockholder
     */
    public @NotNull StockHolder getStockHolder() {
        return this.stockHolder;
    }

    @Override
    public @NotNull String toDebugLog() {
        return this.getClass().getSimpleName() + "{" +
            "uuid=" + this.getStockHolder().getUUID() +
            ", name=" + this.getStockHolder().getName() +
            ", class=" + this.getStockHolder().getClass().getSimpleName() +
            "}";
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
            "stockHolder=" + this.getStockHolder() +
            "}";
    }
}
