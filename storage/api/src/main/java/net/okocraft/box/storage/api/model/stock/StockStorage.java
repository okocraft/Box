package net.okocraft.box.storage.api.model.stock;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.okocraft.box.api.model.stock.StockData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public interface StockStorage {

    @NotNull Collection<StockData> loadStockData(@NotNull UUID uuid) throws Exception;

    void saveStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData) throws Exception;

    void remapItemIds(@NotNull Int2IntMap remappedIdMap) throws Exception;

}
