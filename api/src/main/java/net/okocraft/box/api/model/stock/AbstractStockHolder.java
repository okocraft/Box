package net.okocraft.box.api.model.stock;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.stockholder.StockHolderResetEvent;
import net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockSetEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * An abstract implementation of {@link StockHolder}.
 * <p>
 * The {@link UserStockHolder} returned by
 * {@link net.okocraft.box.api.model.manager.StockManager#loadUserStock(BoxUser)} or
 * {@link net.okocraft.box.api.player.BoxPlayer#getUserStockHolder()}
 * extends this abstract class.
 * @deprecated This class will be removed in Box 6.0.0
 */
@Deprecated(since = "5.5.0", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
public abstract class AbstractStockHolder implements StockHolder {

    public static boolean allowMinus = false;
    private static boolean firstOverflowed = true;

    private static @NotNull ConcurrentMap<BoxItem, AtomicInteger> toStockMap(@NotNull Collection<StockData> stockData) {
        var map = new ConcurrentHashMap<BoxItem, AtomicInteger>(stockData.size());

        for (var data : stockData) {
            var item = BoxProvider.get().getItemManager().getBoxItemOrNull(data.itemId());

            if (item == null) {
                BoxProvider.get().getLogger().warning("Unknown item id: " + data.itemId() + "(amount: " + data.amount() + ")");
                continue;
            }

            map.put(item, new AtomicInteger(data.amount()));
        }

        return map;
    }

    private final ConcurrentMap<BoxItem, AtomicInteger> stockData;

    /**
     * The constructor of {@link AbstractStockHolder}
     *
     * @param stockData the collection of {@link StockData} to restore stock
     */
    protected AbstractStockHolder(@NotNull Collection<StockData> stockData) {
        this.stockData = toStockMap(stockData);
    }

    @Override
    public int getAmount(@NotNull BoxItem item) {
        Objects.requireNonNull(item);
        return Optional.ofNullable(stockData.get(item)).map(AtomicInteger::get).orElse(0);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method calls {@link StockSetEvent} asynchronous.
     *
     * @param item   the item to set the stock quantity
     * @param amount the amount
     */
    @Override
    public void setAmount(@NotNull BoxItem item, int amount, @NotNull StockEvent.Cause cause) {
        Objects.requireNonNull(cause);

        if (amount < 0 && !allowMinus) {
            amount = 0;
        }

        var stock = getStock(item);

        var previous = stock.get();
        stock.set(amount);

        BoxProvider.get().getEventBus().callEventAsync(new StockSetEvent(this, item, amount, previous, cause));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method calls {@link StockIncreaseEvent} asynchronous.
     *
     * @param item      the item to increase the stock
     * @param increment the amount to increase the stock
     * @return the stock quantity after increasing
     */
    @Override
    public int increase(@NotNull BoxItem item, int increment, @NotNull StockEvent.Cause cause) {
        Objects.requireNonNull(cause);

        var stock = getStock(item);
        var amount = stock.addAndGet(increment);

        BoxProvider.get().getEventBus().callEventAsync(new StockIncreaseEvent(this, item, increment, amount, cause));

        if (amount < 0) {
            var logger = BoxProvider.get().getLogger();
            int overflowed = (Integer.MIN_VALUE - amount) * -1;
            logger.warning("The number of " + item.getPlainName() + " that " + getName() + " has is probably overflowing! Actual amount: " + ((long) Integer.MAX_VALUE + overflowed));

            if (firstOverflowed) {
                firstOverflowed = false;
                logger.warning("If overflow occurs during normal usage, please create an issue at https://github.com/okocraft/Box/issues.");
            }
        }

        return amount;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method calls {@link StockDecreaseEvent} asynchronous.
     *
     * @param item      the item to decrease the stock
     * @param decrement the amount to decrease the stock
     * @return the stock quantity after decreasing
     */
    @Override
    public int decrease(@NotNull BoxItem item, int decrement, @NotNull StockEvent.Cause cause) {
        Objects.requireNonNull(cause);

        var stock = getStock(item);
        int decreased = stock.get() - decrement;

        if (0 <= decreased || allowMinus) {
            stock.set(decreased);
        } else {
            stock.set(0);
            decreased = 0;
        }

        BoxProvider.get().getEventBus().callEventAsync(new StockDecreaseEvent(this, item, decrement, decreased, cause));

        return decreased;
    }

    @Override
    public @NotNull @Unmodifiable Collection<BoxItem> getStockedItems() {
        return stockData.entrySet()
                .stream()
                .filter(stockEntry -> 0 < stockEntry.getValue().get())
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public @NotNull @Unmodifiable Collection<StockData> toStockDataCollection() {
        return stockDataStream().toList();
    }

    @Override
    public @NotNull Stream<StockData> stockDataStream() {
        return stockData.entrySet()
                .stream()
                .map(entry -> new StockData(entry.getKey(), entry.getValue().get()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method calls {@link StockHolderResetEvent} asynchronous.
     */
    @Override
    public void reset() {
        var preReset = toStockDataCollection();
        stockData.values().forEach(stock -> stock.set(0));
        BoxProvider.get().getEventBus().callEventAsync(new StockHolderResetEvent(this, preReset));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractStockHolder that = (AbstractStockHolder) o;
        return stockData.equals(that.stockData);
    }

    @Override
    public int hashCode() {
        return stockData.hashCode();
    }

    @Override
    public String toString() {
        return "AbstractStockHolder{" +
                "stockData=" + getStockDataString() +
                '}';
    }

    /**
     * Returns {@link Object#toString()} of stock data map.
     *
     * @return {@link Object#toString()} of stock data map
     */
    protected @NotNull String getStockDataString() {
        return stockData.toString();
    }

    private @NotNull AtomicInteger getStock(@NotNull BoxItem item) {
        Objects.requireNonNull(item);
        return stockData.computeIfAbsent(item, i -> new AtomicInteger(0));
    }
}
