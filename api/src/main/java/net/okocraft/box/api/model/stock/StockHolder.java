package net.okocraft.box.api.model.stock;

import net.okocraft.box.api.model.data.BoxStockData;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

/**
 * An interface that holds stocks.
 */
public interface StockHolder {

    /**
     * Gets the name of this holder.
     *
     * @return the name of this holder
     */
    @NotNull String getName();

    /**
     * Gets the stock quantity of the specified item.
     *
     * @param item the item to get the stock
     * @return the current stock quantity
     */
    int getAmount(@NotNull BoxItem item);

    /**
     * Sets the stock quantity of the specified item.
     *
     * @param item   the item to set the stock quantity
     * @param amount the amount
     */
    void setAmount(@NotNull BoxItem item, int amount);

    /**
     * Increases the stock of the specified item by one.
     *
     * @param item the item to increase the stock
     * @return the stock quantity after increasing
     */
    int increase(@NotNull BoxItem item);

    /**
     * Increases the stock of the specified item.
     *
     * @param item      the item to increase the stock
     * @param increment the amount to increase the stock
     * @return the stock quantity after increasing
     */
    int increase(@NotNull BoxItem item, int increment);

    /**
     * Decreases the stock of the specified item by one.
     *
     * @param item the item to decrease the stock
     * @return the stock quantity after decreasing
     */
    int decrease(@NotNull BoxItem item);

    /**
     * Decreases the stock of the specified item.
     *
     * @param item      the item to decrease the stock
     * @param decrement the amount to decrease the stock
     * @return the stock quantity after decreasing
     */
    int decrease(@NotNull BoxItem item, int decrement);

    /**
     * Gets the current stocks as a {@link BoxStockData} collection.
     *
     * @return the collection of {@link BoxStockData}
     */
    @NotNull @Unmodifiable Collection<BoxStockData> toStockDataCollection();

}
