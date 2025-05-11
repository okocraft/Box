package net.okocraft.box.storage.implementation.database.table;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import net.okocraft.box.storage.api.util.SneakyThrow;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.operator.StockHolderTableOperator;
import net.okocraft.box.storage.implementation.database.operator.StockTableOperator;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
        var stock = new ArrayList<StockData>();

        try (var connection = this.database.getConnection()) {
            int stockId = this.stockHolderTable.getStockHolderIdByUUID(connection, uuid);
            this.stockTable.selectStockById(connection, stockId, stock::add);
        }

        return stock;
    }

    @Override
    public void saveStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData) throws Exception {
        try (var connection = this.database.getConnection()) {
            int stockId = this.stockHolderTable.getStockHolderIdByUUID(connection, uuid);

            this.stockTable.deleteStockByStockId(connection, stockId);

            try (var statement = this.stockTable.insertStockStatement(connection)) {
                for (var data : stockData) {
                    this.stockTable.addInsertStockBatch(statement, stockId, data.itemId(), data.amount());
                }
                statement.executeBatch();
            }
        }
    }

    @Override
    public void remapItemIds(@NotNull Int2IntMap remappedIdMap) throws Exception {
        try (
            var connection = this.database.getConnection();
            var selectByItemIdStatement = this.stockTable.selectStockByItemIdStatement(connection);
            var selectByUUIDAndItemIdStatement = this.stockTable.selectStockByIdAndItemIdStatement(connection);
            var updateItemIdStatement = this.stockTable.updateItemIdStatement(connection);
            var updateAmountStatement = this.stockTable.updateAmountStatement(connection)
        ) {
            for (var remapEntry : remappedIdMap.int2IntEntrySet()) {
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
        try (var connection = this.database.getConnection();
             var statement = this.stockTable.upsertStockStatement(connection)
        ) {
            int stockId = this.stockHolderTable.getStockHolderIdByUUID(connection, uuid);
            for (var data : stockData) {
                this.stockTable.addUpsertStockBatch(statement, stockId, data.itemId(), data.amount());
            }
            statement.executeBatch();
        }
    }

    @Override
    public void cleanupZeroStockData() throws Exception {
        try (var connection = this.database.getConnection()) {
            this.stockTable.deleteZeroStock(connection);
        }
    }

    @Override
    public boolean hasZeroStock() throws Exception {
        try (var connection = this.database.getConnection()) {
            return 0 < this.stockTable.countZeroStock(connection);
        }
    }

    @Override
    public Map<UUID, Collection<StockData>> loadAllStockData() throws Exception {
        Map<UUID, Collection<StockData>> result;
        try (var connection = this.database.getConnection()) {
            var idMap = this.stockHolderTable.getAllUUIDByStockHolderId(connection);
            var invalidUUID = new UUID(0, 0);
            result = new HashMap<>(idMap.size());
            this.stockTable.selectAllStock(connection, (stockId, stockData) -> {
                var uuid = idMap.getOrDefault(stockId.intValue(), invalidUUID);
                if (uuid.equals(invalidUUID)) {
                    return;
                }

                var col = result.computeIfAbsent(uuid, ignored -> new ArrayList<>());
                col.add(stockData);
            });
        }
        return result;
    }

    @Override
    public void saveAllStockData(@NotNull Map<UUID, Collection<StockData>> stockDataMap) throws Exception {
        try (var connection = this.database.getConnection()) {
            this.stockHolderTable.insertStockHolderUUIDs(connection, stockDataMap.keySet());
            var idMap = this.stockHolderTable.getAllStockHolderIdByUUID(connection);
            int count = 0;
            try (var statement = this.stockTable.insertStockStatement(connection)) {
                for (var entry : stockDataMap.entrySet()) {
                    var id = idMap.getInt(entry.getKey());
                    if (id == 0) {
                        continue;
                    }

                    for (var stock : entry.getValue()) {
                        this.stockTable.addInsertStockBatch(statement, id, stock.itemId(), stock.amount());
                        if (count++ == 10000) {
                            statement.executeBatch();
                            count = 0;
                        }
                    }
                }

                if (count != 0) {
                    statement.executeBatch();
                }
            }
        }
    }
}
