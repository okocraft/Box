package net.okocraft.box.api.event.stockholder;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;

/**
 * A {@link StockHolderResetEvent} called when {@link StockHolder} has reset.
 */
public class StockHolderResetEvent extends StockHolderEvent {

    private final Collection<StockData> stockDataBeforeReset;

    /**
     * The constructor of {@link StockHolderResetEvent}.
     *
     * @param stockHolder          the stockholder of the event
     * @param stockDataBeforeReset the {@link StockData} collection before reset
     */
    public StockHolderResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
        super(stockHolder);
        this.stockDataBeforeReset = Collections.unmodifiableCollection(stockDataBeforeReset);
    }

    /**
     * Gets {@link StockHolder#toStockDataCollection()} before reset
     *
     * @return {@link StockHolder#toStockDataCollection()} before reset
     */
    public @NotNull @UnmodifiableView Collection<StockData> getStockDataBeforeReset() {
        return stockDataBeforeReset;
    }

    @Override
    public String toString() {
        return "StockHolderResetEvent{" +
                "stockholder=" + getStockHolder() +
                "}";
    }
}
