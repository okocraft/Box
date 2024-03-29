package net.okocraft.box.test.shared.storage.memory.stock;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MemoryStockStorage implements StockStorage {

    private final Map<UUID, Collection<StockData>> stockDataMap = new HashMap<>();

    @Override
    public void init() {
    }

    @Override
    public @NotNull Collection<StockData> loadStockData(@NotNull UUID uuid) {
        return Objects.requireNonNullElse(this.stockDataMap.get(uuid), Collections.emptyList());
    }

    @Override
    public void saveStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData, @NotNull Int2IntFunction itemIdRemapper) {
        this.stockDataMap.put(
                uuid,
                stockData.stream()
                        .map(data -> new StockData(itemIdRemapper.applyAsInt(data.itemId()), data.amount()))
                        .toList()
        );
    }
}
