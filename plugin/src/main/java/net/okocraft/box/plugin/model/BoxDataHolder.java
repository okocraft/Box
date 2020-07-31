package net.okocraft.box.plugin.model;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.model.item.Stock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BoxDataHolder {

    private final Box plugin;
    private final int internalID;
    private final Set<Stock> itemStock = new HashSet<>();

    public BoxDataHolder(@NotNull Box plugin, int internalID) {
        this.plugin = plugin;
        this.internalID = internalID;
    }

    public int getAmount(@NotNull Item item) {
        return getStock(item).map(Stock::getAmount).orElse(0);
    }

    public void setAmount(@NotNull Item item, int amount) {
        getStockOrCreate(item).setAmount(amount);
    }

    public boolean hasStock(@NotNull Item item) {
        return getStock(item).map(s -> 0 < s.getAmount()).orElse(false);
    }

    public int increase(@NotNull Item item, int increment) {
        int amount = getAmount(item) + increment;
        setAmount(item, amount);
        return amount;
    }

    public int decrease(@NotNull Item item, int decrement) {
        int amount = getAmount(item) + decrement;
        setAmount(item, amount);
        return amount;
    }

    public boolean isAutoStore(@NotNull Item item) {
        return getStock(item).map(Stock::isAutoStore).orElse(false);
    }

    public void setAutoStore(@NotNull Item item, boolean autoStore) {
        getStockOrCreate(item).setAutoStore(autoStore);
    }

    @NotNull
    public Optional<Stock> getStock(@NotNull Item item) {
        return itemStock.stream().filter(s -> s.getItem().equals(item)).findFirst();
    }

    public void setStock(@NotNull Stock stock) {
        itemStock.stream()
                .filter(s -> s.getItem().equals(stock.getItem()))
                .collect(Collectors.toSet())
                .forEach(itemStock::remove);

        itemStock.add(stock);
    }

    @NotNull
    @Unmodifiable
    public Set<Stock> getStocks() {
        return Set.copyOf(itemStock);
    }

    @NotNull
    private Stock getStockOrCreate(@NotNull Item item) {
        Optional<Stock> stock = getStock(item);
        if (stock.isPresent()) {
            return stock.get();
        } else {
            try {
                Stock created = plugin.getStorage().createStock(internalID, item).join();
                setStock(created);
                return created;
            } catch (Throwable e) {
                throw new IllegalStateException("Could not create a stock", e);
            }
        }
    }
}
