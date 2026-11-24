package net.okocraft.box.api.event.stockholder;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A {@link StockHolderResetEvent} called when {@link StockHolder} has reset.
 */
public class StockHolderResetEvent extends StockHolderEvent {

    private final Collection<StockData> stockDataBeforeReset;
    private final StockEvent.Cause cause;

    /**
     * The constructor of {@link StockHolderResetEvent}.
     *
     * @param stockHolder          the stockholder of the event
     * @param stockDataBeforeReset the {@link StockData} collection before reset
     * @param cause                the reason for the reset, including its initiator if known
     */
    public StockHolderResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset,
                                 @NotNull StockEvent.Cause cause) {
        super(stockHolder);
        this.stockDataBeforeReset = Collections.unmodifiableCollection(stockDataBeforeReset);
        this.cause = Objects.requireNonNull(cause, "cause");
    }

    /**
     * Gets {@link StockHolder#toStockDataCollection()} before reset
     *
     * @return {@link StockHolder#toStockDataCollection()} before reset
     */
    public @NotNull @UnmodifiableView Collection<StockData> getStockDataBeforeReset() {
        return this.stockDataBeforeReset;
    }

    /**
     * Gets the cause passed to {@link StockHolder#reset(StockEvent.Cause)}.
     *
     * @return the cause of this reset
     */
    public @NotNull StockEvent.Cause getCause() {
        return this.cause;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "StockHolderResetEvent{" +
            "uuid=" + this.getStockHolder().getUUID() +
            ", name=" + this.getStockHolder().getName() +
            ", class=" + this.getStockHolder().getClass().getSimpleName() +
            ", stockDataBeforeReset={" + this.stockDataBeforeReset.stream().map(data -> data.itemId() + "=" + data.amount()).collect(Collectors.joining(",")) + "}" +
            "}";
    }

    @Override
    public String toString() {
        return "StockHolderResetEvent{" +
            "stockholder=" + this.getStockHolder() +
            ", stockDataBeforeReset=" + this.stockDataBeforeReset +
            "}";
    }
}
