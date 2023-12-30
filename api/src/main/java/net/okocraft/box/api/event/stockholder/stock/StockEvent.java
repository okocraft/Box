package net.okocraft.box.api.event.stockholder.stock;

import net.okocraft.box.api.event.stockholder.StockHolderEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link StockHolderEvent} called when the stock of the {@link StockHolder} is modified.
 * <p>
 * You need to be very careful not to call methods that cause changes to a {@link StockHolder} in the event,
 * as this may result in an infinite loop.
 */
public class StockEvent extends StockHolderEvent {

    private final BoxItem item;
    private final int amount;
    private final Cause cause;

    /**
     * The constructor of {@link StockEvent}.
     *
     * @param stockHolder the stockholder of the event
     * @param item        the item of the stock
     * @param amount      the current amount of the stock
     * @param cause       the cause that indicates why this event was called
     */
    protected StockEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, @NotNull Cause cause) {
        super(stockHolder);
        this.item = Objects.requireNonNull(item);
        this.amount = amount;
        this.cause = Objects.requireNonNull(cause);
    }

    /**
     * Gets the item of the stock.
     *
     * @return the item of the stock
     */
    public @NotNull BoxItem getItem() {
        return item;
    }

    /**
     * Gets the current amount of the stock.
     *
     * @return the current amount of the stock
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Gets the cause that indicates why this event was called.
     *
     * @return the cause that indicates why this event was called
     * @see Cause
     */
    public @NotNull Cause getCause() {
        return cause;
    }

    /**
     * An interface that indicates why the {@link StockEvent} was called.
     * <p>
     * By implementing this interface, {@link Cause} can have additional information.
     * <p>
     * If no additional information is needed, an instance can be created via {@link #create(String)}.
     */
    public interface Cause {

        /**
         * Creates the simple implementation of {@link Cause} that implements {@link #name()}
         *
         * @param name the cause that indicates why the {@link StockEvent} was called
         * @return a {@link Cause} instance
         */
        static @NotNull Cause create(@NotNull String name) {
            return new CauseImpl(name);
        }

        /**
         * Gets the string that indicates the cause.
         * <p>
         * This method returns a string that is a concise representation of the "cause".
         * <p>
         * For example, an API call is "api", and a player's stock deposit/withdrawal is "deposit" or "withdraw".
         *
         * @return the string that indicates the cause
         */
        @NotNull String name();
    }

    private record CauseImpl(@NotNull String name) implements Cause {

        public CauseImpl {
            if (name.isEmpty()) { // implicit null check of "name"
                throw new IllegalArgumentException("name cannot be empty");
            }
        }

    }
}
