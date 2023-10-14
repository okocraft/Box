package net.okocraft.box.core.model.stock;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.UUID;

public class StockHolderImpl extends AbstractStockHolder {

    @Contract("_, _, _ -> new")
    public static @NotNull StockHolder create(@NotNull UUID uuid, @NotNull String name, @NotNull StockEventCaller eventCaller) {
        return new StockHolderImpl(uuid, new NameHolder.Value(name), eventCaller, new Int2ObjectOpenHashMap<>());
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull StockHolder create(@NotNull UUID uuid, @NotNull String name, @NotNull StockEventCaller eventCaller, @NotNull Collection<StockData> stockData) {
        if (stockData.isEmpty()) {
            return create(uuid, name, eventCaller);
        } else {
            var stockMap = new Int2ObjectOpenHashMap<Stock>(stockData.size());

            for (var data : stockData) {
                if (0 < data.amount()) {
                    stockMap.put(data.itemId(), new Stock(data.amount()));
                }
            }

            return new StockHolderImpl(uuid, new NameHolder.Value(name), eventCaller, stockMap);
        }
    }

    @Contract("_, _, _ -> new")
    public static @NotNull StockHolder create(@NotNull BoxUser user, @NotNull Collection<StockData> stockData, @NotNull StockEventCaller eventCaller) {
        if (stockData.isEmpty()) {
            return new StockHolderImpl(user.getUUID(), new NameHolder.FromBoxUser(user), eventCaller, new Int2ObjectOpenHashMap<>());
        } else {
            var stockMap = new Int2ObjectOpenHashMap<Stock>(stockData.size());

            for (var data : stockData) {
                if (0 < data.amount()) {
                    stockMap.put(data.itemId(), new Stock(data.amount()));
                }
            }

            return new StockHolderImpl(user.getUUID(), new NameHolder.FromBoxUser(user), eventCaller, stockMap);
        }
    }

    private final UUID uuid;
    private final NameHolder nameHolder;

    private StockHolderImpl(@NotNull UUID uuid, @NotNull NameHolder nameHolder, @NotNull StockEventCaller eventCaller, @NotNull Int2ObjectOpenHashMap<Stock> stockMap) {
        super(eventCaller, stockMap);
        this.uuid = uuid;
        this.nameHolder = nameHolder;
    }

    @Override
    public @NotNull String getName() {
        return this.nameHolder.get();
    }

    @Override
    public @NotNull UUID getUUID() {
        return this.uuid;
    }

    @Override
    public @NotNull @Unmodifiable Collection<BoxItem> getStockedItems() {
        return this.getStockedItems(BoxProvider.get().getItemManager()::getBoxItemOrNull);
    }

    @Override
    public String toString() {
        var builder =
                new StringBuilder(getClass().getSimpleName())
                        .append("{name=").append(getName())
                        .append(", uuid=").append(this.uuid.toString())
                        .append(", stockMap={");
        this.writeStockMap(builder);
        return builder.append("}}").toString();
    }
}
