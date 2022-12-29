package net.okocraft.box.api.event.stockholder.stock;

import net.okocraft.box.api.event.AsyncEvent;
import net.okocraft.box.api.event.stockholder.StockHolderEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link StockHolderEvent} called when the stock of the {@link StockHolder} is modified.
 * <p>
 * You need to be very careful not to call methods that cause changes to a {@link StockHolder} in the event,
 * as this may result in an infinite loop.
 */
public class StockEvent extends StockHolderEvent implements AsyncEvent {

    private final BoxItem item;
    private final int amount;
    private final Cause cause;

    /**
     * The constructor of {@link StockEvent}.
     *
     * @param stockHolder the stockholder of the event
     * @param item        the item of the stock
     * @param amount      the current amount of the stock
     * @deprecated use {@link #StockEvent(StockHolder, BoxItem, int, Cause)}}
     */
    @Deprecated(forRemoval = true, since = "5.2.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.3.0")
    public StockEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount) {
        this(stockHolder, item, amount, Cause.API);
    }

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
        this.cause = cause;
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
     */
    public @NotNull Cause getCause() {
        return cause;
    }

    /**
     * An interface that indicates why the {@link StockEvent} was called
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
         * TODO
         */
        Cause API = create("api");

        /**
         * Gets the string that indicates the cause.
         *
         * @return TODO
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
