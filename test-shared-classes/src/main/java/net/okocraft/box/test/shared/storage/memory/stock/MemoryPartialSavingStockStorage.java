package net.okocraft.box.test.shared.storage.memory.stock;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryPartialSavingStockStorage implements PartialSavingStockStorage {

    private final Map<UUID, Int2IntMap> stockDataMap = new HashMap<>();

    @Override
    public @NotNull Collection<StockData> loadStockData(@NotNull UUID uuid) {
        var map = this.stockDataMap.get(uuid);

        if (map == null) {
            return Collections.emptyList();
        }

        var result = new ArrayList<StockData>(map.size());

        for (var entry : map.int2IntEntrySet()) {
            result.add(new StockData(entry.getIntKey(), entry.getIntValue()));
        }

        return result;
    }

    @Override
    public void saveStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData) {
        var map = new Int2IntOpenHashMap(stockData.size());

        for (var data : stockData) {
            map.put(data.itemId(), data.amount());
        }

        this.stockDataMap.put(uuid, map);
    }

    @Override
    public void remapItemIds(@NotNull Int2IntMap remappedIdMap) {
        for (var map : this.stockDataMap.values()) {
            new Int2IntOpenHashMap(map)
                .int2IntEntrySet()
                .forEach(entry -> map.put(remappedIdMap.getOrDefault(entry.getIntKey(), entry.getIntValue()), entry.getIntValue()));
        }
    }

    @Override
    public void savePartialStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData) {
        var map = this.stockDataMap.computeIfAbsent(uuid, ignored -> new Int2IntOpenHashMap());

        for (var data : stockData) {
            if (data.amount() < 1) {
                map.remove(data.itemId());
            } else {
                map.put(data.itemId(), data.amount());
            }
        }
    }

    @Override
    public void cleanupZeroStockData() {
        for (var stockMap : this.stockDataMap.values()) {
            stockMap.values().removeIf(amount -> amount == 0);
        }
    }

    @Override
    public boolean hasZeroStock() {
        return this.stockDataMap.values().stream()
            .map(Int2IntMap::values)
            .flatMapToInt(IntCollection::intStream)
            .anyMatch(amount -> amount == 0);
    }
}
