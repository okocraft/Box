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
import java.util.UUID;

// | uuid | item_id | amount |
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
            this.stockTable.selectStockByUUID(connection, uuid, stock::add);
        }

        return stock;
    }

    @Override
    public void saveStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData) throws Exception {
        try (var connection = this.database.getConnection()) {
            var strUuid = uuid.toString();

            this.stockTable.deleteStockByUUID(connection, strUuid);

            try (var statement = this.stockTable.insertStockStatement(connection)) {
                for (var data : stockData) {
                    this.stockTable.addInsertStockBatch(statement, strUuid, data.itemId(), data.amount());
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
            var selectByUUIDAndItemIdStatement = this.stockTable.selectStockByUUIDAndItemIdStatement(connection);
            var updateItemIdStatement = this.stockTable.updateItemIdStatement(connection);
            var updateAmountStatement = this.stockTable.updateAmountStatement(connection)
        ) {
            for (var remapEntry : remappedIdMap.int2IntEntrySet()) {
                int oldItemId = remapEntry.getIntKey();
                int newItemId = remapEntry.getIntValue();
                this.stockTable.selectStockByItemId(
                    selectByItemIdStatement,
                    oldItemId,
                    (uuid, amount) -> {
                        try {
                            this.stockTable.selectStockByUUIDAndItemId(
                                selectByUUIDAndItemIdStatement,
                                uuid,
                                newItemId,
                                existingAmount -> {
                                    try {
                                        this.stockTable.addUpdateAmountBatch(updateAmountStatement, uuid, newItemId, amount + existingAmount);
                                    } catch (SQLException e) {
                                        SneakyThrow.sneaky(e);
                                    }
                                },
                                () -> {
                                    try {
                                        this.stockTable.addUpdateItemIdBatch(updateItemIdStatement, uuid, oldItemId, newItemId);
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
            var strUuid = uuid.toString();
            for (var data : stockData) {
                this.stockTable.addUpsertStockBatch(statement, strUuid, data.itemId(), data.amount());
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
}
