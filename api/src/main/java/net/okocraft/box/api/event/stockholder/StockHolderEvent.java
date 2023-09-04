package net.okocraft.box.api.event.stockholder;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.stock.UserStockHolder;
import org.jetbrains.annotations.ApiStatus;
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
        return stockHolder;
    }

    /**
     * Checks if the {@link StockHolder} of this event is a {@link UserStockHolder}.
     *
     * @return whether the {@link StockHolder} is a {@link UserStockHolder} or not
     * @deprecated {@link UserStockHolder} will be removed in Box 6.0.0
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    public boolean isUserStockHolder() {
        return stockHolder instanceof UserStockHolder;
    }

    /**
     * Gets the {@link StockHolder} as a {@link UserStockHolder}.
     * <p>
     * You must check if the {@link StockHolder} of this event
     * is {@link UserStockHolder} using {@link #isUserStockHolder()}
     * before calling this method.
     *
     * @return the {@link UserStockHolder}
     * @throws IllegalStateException if the {@link StockHolder} of this event is not {@link UserStockHolder}
     * @deprecated {@link UserStockHolder} will be removed in Box 6.0.0
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    public @NotNull UserStockHolder getUserStockHolder() {
        if (stockHolder instanceof UserStockHolder userStockHolder) {
            return userStockHolder;
        } else {
            throw new IllegalStateException("The StockHolder of this event is not UserStockHolder.");
        }
    }

    @Override
    public @NotNull String toDebugLog() {
        return getEventName() + "{" +
                "uuid=" + getStockHolder().getUUID() +
                ", name=" + getStockHolder().getName() +
                ", class=" + getStockHolder().getClass().getSimpleName() +
                "}";
    }
}
