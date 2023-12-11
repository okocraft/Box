package net.okocraft.box.api.model.stock;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

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
     * @throws NullPointerException if {@code item} is {@code null}
     */
    default int getAmount(@NotNull BoxItem item) {
        return getAmount(item.getInternalId());
    }

    /**
     * Gets the stock quantity of the specified item.
     *
     * @param itemId the id of the {@link BoxItem} to get the stock
     * @return the current stock quantity
     * @throws NullPointerException if {@code item} is {@code null}
     */
    int getAmount(int itemId);

    /**
     * Sets the stock quantity of the specified item.
     *
     * @param item   the item to set the stock quantity
     * @param amount the amount
     * @param cause  the cause that indicates why this method called
     * @throws IllegalArgumentException if {@code amount} is negative
     * @throws NullPointerException     if {@code item} or {@code cause} is {@code null}
     */
    void setAmount(@NotNull BoxItem item, @Range(from = 0, to = Integer.MAX_VALUE) int amount, @NotNull StockEvent.Cause cause);

    /**
     * Increases the stock of the specified item.
     * <p>
     * If {@code increment} is zero, this method returns {@link #getAmount(BoxItem)}.
     * <p>
     * The behavior when the stock has overflowed depends on the implementation.
     *
     * @param item      the item to increase the stock
     * @param increment the amount to increase the stock
     * @param cause     the cause that indicates why this method called
     * @return the stock quantity after increasing
     * @throws IllegalArgumentException if {@code increment} is negative
     * @throws NullPointerException     if {@code item} or {@code cause} is {@code null}
     */
    int increase(@NotNull BoxItem item, @Range(from = 0, to = Integer.MAX_VALUE) int increment, @NotNull StockEvent.Cause cause);

    /**
     * Decreases the stock of the specified item.
     * <p>
     * If {@code decrement} is zero, this method returns {@link #getAmount(BoxItem)}.
     * <p>
     * If the current stock is less than {@code decrement}, this method sets the stock to zero.
     * In other words, this method does not guarantee that the stock is always decreased by {@code decrement}.
     * <p>
     * If you want to decrease the stock when it is sufficient, use {@link #decreaseIfPossible(BoxItem, int, StockEvent.Cause)}.
     *
     * @param item      the item to decrease the stock
     * @param decrement the amount to decrease the stock
     * @param cause     the cause that indicates why this method called
     * @return the stock quantity after decreasing
     * @throws IllegalArgumentException if {@code decrement} is negative
     * @throws NullPointerException     if {@code item} or {@code cause} is {@code null}
     */
    int decrease(@NotNull BoxItem item, @Range(from = 0, to = Integer.MAX_VALUE) int decrement, @NotNull StockEvent.Cause cause);

    /**
     * Decreases the stock of the specified item.
     * <p>
     * This method has the following specification:
     * <p>
     * <ul>
     * <li>If the stock is less than {@code limit}, this method sets the stock to zero and returns the stock before set to zero</li>
     * <li>Otherwise, this method decreases the stock by {@code limit} and returns {@code limit}</li>
     * </ul>
     * <p>
     * This method is useful when you want to decrease the stock as much as possible, with {@code limit} as the maximum decrement.
     *
     * @param item  the item to decrease the stock
     * @param limit the maximum decrement
     * @param cause the cause that indicates why this method called
     * @return the amount of decrement
     * @throws IllegalArgumentException if {@code limit} is negative
     * @throws NullPointerException     if {@code item} or {@code cause} is {@code null}
     */
    int decreaseToZero(@NotNull BoxItem item, @Range(from = 0, to = Integer.MAX_VALUE) int limit, @NotNull StockEvent.Cause cause);

    /**
     * Decreases the stock of the specified item.
     * <p>
     * If the stock is less than the {@code decrement}, this method does not actually decrease it, and returns {@code -1}.
     *
     * @param item      the item to decrease the stock
     * @param decrement the amount to decrease the stock
     * @param cause     the cause that indicates why this method called
     * @return the stock quantity after decreasing or {@code -1} when the stock is less than the {@code decrement}
     * @throws IllegalArgumentException if {@code decrement} is negative
     * @throws NullPointerException     if {@code item} or {@code cause} is {@code null}
     */
    int decreaseIfPossible(@NotNull BoxItem item, @Range(from = 0, to = Integer.MAX_VALUE) int decrement, @NotNull StockEvent.Cause cause);

    /**
     * Decreases the stock of the specified items.
     *
     * @param decrementMap the map (key: {@link BoxItem}, value: decrement)
     * @param cause        the cause that indicates why this method called
     * @return {@code true} if stock of all specified items is decreased, otherwise {@code false}
     * @throws IllegalArgumentException if the negative value is contained in {@code decrementMap}
     * @throws NullPointerException     if {@code decrementMap} or {@code cause} is {@code null}
     * @see #decreaseIfPossible(Object2IntMap, StockEvent.Cause)
     */
    @ApiStatus.NonExtendable
    default boolean decreaseIfPossible(@NotNull Map<BoxItem, Integer> decrementMap, @NotNull StockEvent.Cause cause) {
        var map = new Object2IntArrayMap<BoxItem>(decrementMap.size());

        for (var entry : decrementMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().intValue());
        }

        return decreaseIfPossible(map, cause);
    }

    /**
     * Decreases the stock of the specified items.
     * <p>
     * This method is implemented as follows:
     *
     * <ul>
     *     <li>If all items in the {@code decrementMap} are greater than the decrement (the value keyed by {@link BoxItem}), this method will actually decrease them and return {@code true}.</li>
     *     <li>If even one item is lacking, it returns {@code false} without any stock modification.</li>
     *     <li>If {@code decrementMap} is empty, this method returns {@code true}.</li>
     *     <li>When the negative value is contained in {@code decrementMap}, this method throws {@link IllegalArgumentException}</li>
     *     <li>When the decrement value is zero, this method ignores it.</li>
     * </ul>
     *
     * @param decrementMap the map (key: {@link BoxItem}, value: decrement)
     * @param cause        the cause that indicates why this method called
     * @return {@code true} if stock of all specified items is decreased, otherwise {@code false}
     * @throws IllegalArgumentException if the negative value is contained in {@code decrementMap}
     * @throws NullPointerException     if {@code decrementMap} or {@code cause} is {@code null}
     */
    boolean decreaseIfPossible(@NotNull Object2IntMap<BoxItem> decrementMap, @NotNull StockEvent.Cause cause);

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
     * Resets all stock.
     *
     * @return the collection of {@link StockData} before reset
     */
    @NotNull @Unmodifiable Collection<StockData> reset();
}
