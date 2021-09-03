package net.okocraft.box.api.event.stock;

import com.github.siroshun09.event4j.event.Event;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.stock.UserStockHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A class that represents a {@link StockHolder} related event.
 * <p>
 * This event is triggered when a {@link StockHolder} is modified.
 * <p>
 * Therefore, you need to be very careful not to call methods
 * that cause changes to a {@link StockHolder} in the event, as this may result in an infinite loop.
 */
public class StockEvent extends Event {

    private final StockHolder stockHolder;

    /**
     * The constructor of {@link StockEvent}.
     *
     * @param stockHolder the stockholder of the event
     */
    public StockEvent(@NotNull StockHolder stockHolder) {
        this.stockHolder = stockHolder;
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
     */
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
     * @throws IllegalStateException if the {@link StockEvent} of this event is not {@link UserStockHolder}
     */
    public @NotNull UserStockHolder getUserStockHolder() {
        if (stockHolder instanceof UserStockHolder userStockHolder) {
            return userStockHolder;
        } else {
            throw new IllegalStateException("The StockHolder of this event is not UserStockHolder.");
        }
    }
}
