package net.okocraft.box.plugin.model;

import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.model.item.Stock;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BoxDataHolder {

    private final Set<Stock> itemStock = new HashSet<>();

    public int getAmount(@NotNull Item item) {
        return getStock(item).map(Stock::getAmount).orElse(0);
    }

    public void setAmount(@NotNull Item item, int amount) {
        getStockOrCreate(item).setAmount(amount);
    }

    public Supplier<Integer> increase(@NotNull Item item) {
        return increase(item, 1);
    }

    public Supplier<Integer> increase(@NotNull Item item, int increment) {
        int amount = getAmount(item) + increment;
        setAmount(item, amount);
        return () -> getAmount(item);
    }

    public Supplier<Integer> decrease(@NotNull Item item) {
        return decrease(item, 1);
    }

    public Supplier<Integer> decrease(@NotNull Item item, int decrement) {
        int amount = getAmount(item) + decrement;
        setAmount(item, amount);
        return () -> getAmount(item);
    }

    public boolean isAutoStore(@NotNull Item item) {
        return getStock(item).map(Stock::isAutoStore).orElse(false);
    }

    public void setStock(@NotNull Stock stock) {
        itemStock.stream()
                .filter(s -> s.getItem().equals(stock.getItem()))
                .collect(Collectors.toSet())
                .forEach(itemStock::remove);

        itemStock.add(stock);
    }

    public void setAutoStore(@NotNull Item item, boolean autoStore) {
        getStockOrCreate(item).setAutoStore(autoStore);
    }

    @NotNull
    private Optional<Stock> getStock(@NotNull Item item) {
        return itemStock.stream().filter(s -> s.getItem().equals(item)).findFirst();
    }

    @NotNull
    private Stock getStockOrCreate(@NotNull Item item) {
        Optional<Stock> stock = getStock(item);
        if (stock.isPresent()) {
            return stock.get();
        } else {
            Stock created = createStock(item);
            setStock(created);
            return created;
        }
    }

    @Contract(value = "_ -> new", pure = true)
    private @NotNull Stock createStock(@NotNull Item item) {
        return new Stock(0, item); // TODO: Item Table に実装する
    }
}
