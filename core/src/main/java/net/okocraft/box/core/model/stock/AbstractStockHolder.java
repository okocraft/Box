package net.okocraft.box.core.model.stock;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stock.StockSetEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class AbstractStockHolder implements StockHolder {

    private static final Collector<StockData, ?, ConcurrentMap<BoxItem, AtomicInteger>> TO_MAP =
            Collectors.toConcurrentMap(StockData::item, data -> new AtomicInteger(data.amount()));

    private final ConcurrentMap<BoxItem, AtomicInteger> stockData;

    protected AbstractStockHolder(@NotNull Collection<StockData> stockData) {
        Objects.requireNonNull(stockData);
        this.stockData = stockData.stream().collect(TO_MAP);
    }

    @Override
    public int getAmount(@NotNull BoxItem item) {
        return Optional.ofNullable(stockData.get(item)).map(AtomicInteger::get).orElse(0);
    }

    @Override
    public void setAmount(@NotNull BoxItem item, int amount) {
        var stock = getStock(item);

        var previous = stock.get();
        stock.set(amount);

        BoxProvider.get().getEventBus().callEvent(new StockSetEvent(this, item, amount, previous));
    }

    @Override
    public int increase(@NotNull BoxItem item) {
        return increase(item, 1);
    }

    @Override
    public int increase(@NotNull BoxItem item, int increment) {
        var amount = getStock(item).addAndGet(increment);

        BoxProvider.get().getEventBus().callEvent(new StockIncreaseEvent(this, item, increment, amount));

        if (amount < 0) {
            BoxProvider.get().getLogger()
                    .warning(item.getPlainName() + " is negative number: " + amount + " (" + getName() + ")");
        }

        return amount;
    }

    @Override
    public int decrease(@NotNull BoxItem item) {
        return decrease(item, 1);
    }

    @Override
    public int decrease(@NotNull BoxItem item, int decrement) {
        var amount = getStock(item).addAndGet(-decrement);

        BoxProvider.get().getEventBus().callEvent(new StockDecreaseEvent(this, item, decrement, amount));

        if (amount < 0) {
            BoxProvider.get().getLogger()
                    .warning(item.getPlainName() + " is negative number: " + amount + " (" + getName() + ")");
        }

        return amount;
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
    public @NotNull Collection<StockData> toStockDataCollection() {
        return stockData.entrySet()
                .stream()
                .map(entry -> new StockData(entry.getKey(), entry.getValue().get()))
                .toList();
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

    protected @NotNull String getStockDataString() {
        return stockData.toString();
    }

    private @NotNull AtomicInteger getStock(@NotNull BoxItem item) {
        return stockData.computeIfAbsent(item, i -> new AtomicInteger(0));
    }
}
