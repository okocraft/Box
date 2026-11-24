package net.okocraft.box.api.event.stockholder;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A {@link StockHolderResetEvent} called when {@link StockHolder} has reset.
 */
public class StockHolderResetEvent extends StockHolderEvent {

    public static final StockEvent.Cause DEFAULT_CAUSE = StockEvent.Cause.create("reset");

    private final Collection<StockData> stockDataBeforeReset;
    private final StockEvent.Cause cause;
    private final Instant occurredAt = Instant.now();

    /**
     * The constructor of {@link StockHolderResetEvent}.
     *
     * @param stockHolder          the stockholder of the event
     * @param stockDataBeforeReset the {@link StockData} collection before reset
     */
    public StockHolderResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
        this(stockHolder, stockDataBeforeReset, DEFAULT_CAUSE);
    }

    public StockHolderResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset,
                                 StockEvent.@NotNull Cause cause) {
        super(stockHolder);
        this.stockDataBeforeReset = Collections.unmodifiableCollection(stockDataBeforeReset);
        this.cause = Objects.requireNonNull(cause);
    }

    public StockEvent.@NotNull Cause getCause() {
        return this.cause;
    }

    public @NotNull Instant getOccurredAt() {
        return this.occurredAt;
    }

    /**
     * Gets {@link StockHolder#toStockDataCollection()} before reset
     *
     * @return {@link StockHolder#toStockDataCollection()} before reset
     */
    public @NotNull @UnmodifiableView Collection<StockData> getStockDataBeforeReset() {
        return this.stockDataBeforeReset;
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
