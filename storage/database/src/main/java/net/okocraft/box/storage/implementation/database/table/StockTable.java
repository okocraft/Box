package net.okocraft.box.storage.implementation.database.table;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import net.okocraft.box.storage.api.util.SneakyThrow;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.operator.StockHolderTableOperator;
import net.okocraft.box.storage.implementation.database.operator.StockTableOperator;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// | stock_id | item_id | amount |
public class StockTable implements PartialSavingStockStorage {

    private final Database database;
    private final StockHolderTableOperator stockHolderTable;
    private final StockTableOperator stockTable;

    public StockTable(@NotNull Database database) {
        this.database = database;
        this.stockHolderTable = database.operators().stockHolderTable();
        this.stockTable = database.operators().stockTable();
    }

    public void init(@NotNull Connection connection) throws Exception {
        this.stockHolderTable.initTable(connection);
        this.stockTable.initTable(connection);
    }

    @Override
    public @NotNull Collection<StockData> loadStockData(@NotNull UUID uuid) throws Exception {
        List<StockData> stock = new ArrayList<>();

        try (Connection connection = this.database.getConnection()) {
            int stockId = this.stockHolderTable.getStockHolderIdByUUID(connection, uuid);
            this.stockTable.selectStockById(connection, stockId, stock::add);
        }

        return stock;
    }

    @Override
    public void saveStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData) throws Exception {
        try (Connection connection = this.database.getConnection()) {
            int stockId = this.stockHolderTable.getStockHolderIdByUUID(connection, uuid);

            this.stockTable.deleteStockByStockId(connection, stockId);

            try (PreparedStatement statement = this.stockTable.insertStockStatement(connection)) {
                for (StockData data : stockData) {
                    this.stockTable.addInsertStockBatch(statement, stockId, data.itemId(), data.amount());
                }
                statement.executeBatch();
            }
        }
    }

    @Override
    public void remapItemIds(@NotNull Int2IntMap remappedIdMap) throws Exception {
        try (
            Connection connection = this.database.getConnection();
            PreparedStatement selectByItemIdStatement = this.stockTable.selectStockByItemIdStatement(connection);
            PreparedStatement selectByUUIDAndItemIdStatement = this.stockTable.selectStockByIdAndItemIdStatement(connection);
            PreparedStatement updateItemIdStatement = this.stockTable.updateItemIdStatement(connection);
            PreparedStatement updateAmountStatement = this.stockTable.updateAmountStatement(connection)
        ) {
            for (Int2IntMap.Entry remapEntry : remappedIdMap.int2IntEntrySet()) {
                int oldItemId = remapEntry.getIntKey();
                int newItemId = remapEntry.getIntValue();
                this.stockTable.selectStockByItemId(
                    selectByItemIdStatement,
                    oldItemId,
                    (stockId, amount) -> {
                        try {
                            this.stockTable.selectStockByIdAndItemId(
                                selectByUUIDAndItemIdStatement,
                                stockId,
                                newItemId,
                                existingAmount -> {
                                    try {
                                        this.stockTable.addUpdateAmountBatch(updateAmountStatement, stockId, newItemId, amount + existingAmount);
                                    } catch (SQLException e) {
                                        SneakyThrow.sneaky(e);
                                    }
                                },
                                () -> {
                                    try {
                                        this.stockTable.addUpdateItemIdBatch(updateItemIdStatement, stockId, oldItemId, newItemId);
                                    } catch (SQLException e) {
                                        SneakyThrow.sneaky(e);
                                    }
                                }
                            );
                        } catch (SQLException e) {
                            SneakyThrow.sneaky(e);
                        }
                    }
                );
            }

            updateItemIdStatement.executeBatch();
            updateAmountStatement.executeBatch();
            this.stockTable.deleteStockByItemIds(connection, remappedIdMap.keySet().intStream());
        }
    }

    @Override
    public void savePartialStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData) throws Exception {
        try (Connection connection = this.database.getConnection();
             PreparedStatement statement = this.stockTable.upsertStockStatement(connection)
        ) {
            int stockId = this.stockHolderTable.getStockHolderIdByUUID(connection, uuid);
            for (StockData data : stockData) {
                this.stockTable.addUpsertStockBatch(statement, stockId, data.itemId(), data.amount());
            }
            statement.executeBatch();
        }
    }

    @Override
    public void cleanupZeroStockData() throws Exception {
        try (Connection connection = this.database.getConnection()) {
            this.stockTable.deleteZeroStock(connection);
        }
    }

    @Override
    public boolean hasZeroStock() throws Exception {
        try (Connection connection = this.database.getConnection()) {
            return 0 < this.stockTable.countZeroStock(connection);
        }
    }

    @Override
    public Map<UUID, Collection<StockData>> loadAllStockData() throws Exception {
        Map<UUID, Collection<StockData>> result;
        try (Connection connection = this.database.getConnection()) {
            Int2ObjectMap<UUID> idMap = this.stockHolderTable.getAllUUIDByStockHolderId(connection);
            UUID invalidUUID = new UUID(0, 0);
            result = new HashMap<>(idMap.size());
            this.stockTable.selectAllStock(connection, (stockId, stockData) -> {
                UUID uuid = idMap.getOrDefault(stockId.intValue(), invalidUUID);
                if (uuid.equals(invalidUUID)) {
                    return;
                }

                Collection<StockData> col = result.computeIfAbsent(uuid, ignored -> new ArrayList<>());
                col.add(stockData);
            });
        }
        return result;
    }

    @Override
    public void saveAllStockData(@NotNull Map<UUID, Collection<StockData>> stockDataMap) throws Exception {
        try (Connection connection = this.database.getConnection()) {
            List<UUID> uuids = List.copyOf(stockDataMap.keySet());
            for (int i = 0; i * 10000 < uuids.size(); i++) {
                List<UUID> sublist = uuids.subList(i * 10000, Math.min((i + 1) * 10000, uuids.size()));
                this.stockHolderTable.insertStockHolderUUIDs(connection, sublist);
            }

            Object2IntMap<UUID> idMap = this.stockHolderTable.getAllStockHolderIdByUUID(connection);
            int count = 0;
            List<StockTableOperator.StockRecord> records = new ArrayList<>(Math.min(10000, stockDataMap.size()));
            for (Map.Entry<UUID, Collection<StockData>> entry : stockDataMap.entrySet()) {
                int id = idMap.getInt(entry.getKey());
                if (id == 0) {
                    continue;
                }

                for (StockData stock : entry.getValue()) {
                    records.add(new StockTableOperator.StockRecord(id, stock.itemId(), stock.amount()));
                    if (++count == 10000) {
                        this.stockTable.insertStockRecords(connection, records);
                        records.clear();
                        count = 0;
                    }
                }
            }

            if (count != 0) {
                this.stockTable.insertStockRecords(connection, records);
            }
        }
    }
}
