package net.okocraft.box.core.model.stock;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.util.Debugger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class AbstractStockHolder implements StockHolder {

    private static final Collector<StockData, ?, Map<BoxItem, AtomicInteger>> TO_MAP;

    static {
        TO_MAP = Collectors.toMap(StockData::item, data -> new AtomicInteger(data.amount()));
    }

    private final Map<BoxItem, AtomicInteger> stockData;

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
        Debugger.log(() -> "SET: " + item.getPlainName() + ": " + amount + " (" + getName() + ")");
        getStock(item).set(amount);
    }

    @Override
    public int increase(@NotNull BoxItem item) {
        return increase(item, 1);
    }

    @Override
    public int increase(@NotNull BoxItem item, int increment) {
        var amount = getStock(item).addAndGet(increment);

        Debugger.log(() ->
                "INCREASE (" + increment + ")" + ": " +
                        item.getPlainName() + ": " + amount + " (" + getName() + ")");

        return amount;
    }

    @Override
    public int decrease(@NotNull BoxItem item) {
        return decrease(item, 1);
    }

    @Override
    public int decrease(@NotNull BoxItem item, int decrement) {
        var amount = getStock(item).addAndGet(-decrement);

        Debugger.log(() ->
                "DECREASE (" + decrement + ")" + ": " +
                        item.getPlainName() + ": " + amount + " (" + getName() + ")");

        return amount;
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
