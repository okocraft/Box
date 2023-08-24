package net.okocraft.box.api.model.stock;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * An interface that holds stock.
 * <p>
 * The implementation of this interface must be thread-safe.
 */
public interface StockHolder {

    /**
     * Gets the name of this holder.
     *
     * @return the name of this holder
     */
    @NotNull String getName();

    /**
     * Gets the uuid of this holder.
     *
     * @return the uuid of this holder
     */
    @NotNull UUID getUUID();

    /**
     * Gets the stock quantity of the specified item.
     *
     * @param item the item to get the stock
     * @return the current stock quantity
     */
    int getAmount(@NotNull BoxItem item);

    /**
     * Sets the stock quantity of the specified item.
     * <p>
     * This method calls {@link #setAmount(BoxItem, int, StockEvent.Cause)} with {@link StockEvent.Cause#API}.
     *
     * @param item   the item to set the stock quantity
     * @param amount the amount
     * @deprecated use {@link #setAmount(BoxItem, int, StockEvent.Cause)}
     */
    @ApiStatus.NonExtendable
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default void setAmount(@NotNull BoxItem item, int amount) {
        setAmount(item, amount, StockEvent.Cause.API);
    }

    /**
     * Sets the stock quantity of the specified item.
     *
     * @param item   the item to set the stock quantity
     * @param amount the amount
     * @param cause  the cause that indicates why this method called
     */
    default void setAmount(@NotNull BoxItem item, int amount, @NotNull StockEvent.Cause cause) {
        setAmount(item, amount);
    }

    /**
     * Increases the stock of the specified item by one.
     * <p>
     * This method calls {@link #increase(BoxItem, int, StockEvent.Cause)} with {@link StockEvent.Cause#API}.
     *
     * @param item the item to increase the stock
     * @return the stock quantity after increasing
     * @deprecated use {@link #increase(BoxItem, int, StockEvent.Cause)}
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default int increase(@NotNull BoxItem item) {
        return increase(item, 1, StockEvent.Cause.API);
    }

    /**
     * Increases the stock of the specified item.
     * <p>
     * This method calls {@link #increase(BoxItem, int, StockEvent.Cause)} with {@link StockEvent.Cause#API}.
     *
     * @param item      the item to increase the stock
     * @param increment the amount to increase the stock
     * @return the stock quantity after increasing
     * @deprecated use {@link #increase(BoxItem, int, StockEvent.Cause)}
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default int increase(@NotNull BoxItem item, int increment) {
        return increase(item, increment, StockEvent.Cause.API);
    }

    /**
     * Increases the stock of the specified item.
     *
     * @param item      the item to increase the stock
     * @param increment the amount to increase the stock
     * @param cause     the cause that indicates why this method called
     * @return the stock quantity after increasing
     */
    default int increase(@NotNull BoxItem item, int increment, @NotNull StockEvent.Cause cause) {
        return increase(item, increment);
    }

    /**
     * Decreases the stock of the specified item by one.
     * <p>
     * This method calls {@link #decrease(BoxItem, int, StockEvent.Cause)} with {@link StockEvent.Cause#API}.
     *
     * @param item the item to decrease the stock
     * @return the stock quantity after decreasing
     * @deprecated use {@link #decrease(BoxItem, int, StockEvent.Cause)}
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default int decrease(@NotNull BoxItem item) {
        return decrease(item, 1, StockEvent.Cause.API);
    }

    /**
     * Decreases the stock of the specified item.
     * <p>
     * This method calls {@link #decrease(BoxItem, int, StockEvent.Cause)} with {@link StockEvent.Cause#API}.
     *
     * @param item      the item to decrease the stock
     * @param decrement the amount to decrease the stock
     * @return the stock quantity after decreasing
     * @deprecated use {@link #decrease(BoxItem, int, StockEvent.Cause)}
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default int decrease(@NotNull BoxItem item, int decrement) {
        return decrease(item, decrement, StockEvent.Cause.API);
    }

    /**
     * Decreases the stock of the specified item.
     *
     * @param item      the item to decrease the stock
     * @param decrement the amount to decrease the stock
     * @param cause     the cause that indicates why this method called
     * @return the stock quantity after decreasing
     */
    default int decrease(@NotNull BoxItem item, int decrement, @NotNull StockEvent.Cause cause) {
        return decrease(item, decrement);
    }

    /**
     * Gets a stocked item collection.
     *
     * @return a stocked item collection.
     */
    @NotNull @Unmodifiable Collection<BoxItem> getStockedItems();

    /**
     * Gets the current stock as a {@link StockData} collection.
     *
     * @return the collection of {@link StockData}
     */
    @NotNull @Unmodifiable Collection<StockData> toStockDataCollection();

    /**
     * Gets the {@link StockData} stream.
     *
     * @return the stream of {@link StockData}.
     * @deprecated use {@link #toStockDataCollection()}
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default @NotNull Stream<StockData> stockDataStream() {
        return toStockDataCollection().stream();
    }

    /**
     * Resets all stock.
     */
    void reset();

}
