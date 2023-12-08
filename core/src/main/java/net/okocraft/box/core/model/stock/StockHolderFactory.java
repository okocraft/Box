package net.okocraft.box.core.model.stock;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.function.IntFunction;

public final class StockHolderFactory {

    @Contract("_, _, _, _, _ -> new")
    public static @NotNull StockHolder create(@NotNull UUID uuid, @NotNull String name, @NotNull StockEventCaller eventCaller, @NotNull Collection<StockData> stockData, @NotNull IntFunction<BoxItem> toBoxItem) {
        return create(uuid, new NameHolder.Value(name), eventCaller, stockData, toBoxItem);
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull StockHolder create(@NotNull BoxUser user, @NotNull StockEventCaller eventCaller, @NotNull Collection<StockData> stockData, @NotNull IntFunction<BoxItem> toBoxItem) {
        return create(user.getUUID(), new NameHolder.FromBoxUser(user), eventCaller, stockData, toBoxItem);
    }

    private static @NotNull StockHolder create(@NotNull UUID uuid, @NotNull NameHolder nameHolder, @NotNull StockEventCaller eventCaller, @NotNull Collection<StockData> stockData, @NotNull IntFunction<BoxItem> toBoxItem) {
        if (stockData.isEmpty()) {
            return new StockHolderImpl(uuid, nameHolder, eventCaller, new Int2ObjectOpenHashMap<>(), toBoxItem);
        } else {
            var stockMap = new Int2ObjectOpenHashMap<Stock>(stockData.size());

            for (var data : stockData) {
                if (0 < data.amount()) {
                    stockMap.put(data.itemId(), new Stock(data.amount()));
                }
            }

            return new StockHolderImpl(uuid, nameHolder, eventCaller, stockMap, toBoxItem);
        }
    }

    private StockHolderFactory() {
        throw new UnsupportedOperationException();
    }
}
