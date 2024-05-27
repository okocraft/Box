package net.okocraft.box.storage.implementation.database.table;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.database.mysql.MySQLDatabase;
import net.okocraft.box.storage.implementation.database.database.sqlite.SQLiteDatabase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

// | uuid | item_id | amount |
public class StockTable extends AbstractTable implements PartialSavingStockStorage {

    public StockTable(@NotNull Database database) {
        super(database, database.getSchemaSet().stockTable());
    }

    @Override
    public void init() throws Exception {
        this.createTableAndIndex();
    }

    @Override
    public @NotNull Collection<StockData> loadStockData(@NotNull UUID uuid) throws Exception {
        var stock = new ArrayList<StockData>();

        try (var connection = this.database.getConnection();
             var statement = this.prepareStatement(connection, "SELECT `item_id`, `amount` FROM `%table%` WHERE `uuid`=?")) {
            var strUuid = uuid.toString();
            statement.setString(1, strUuid);

            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int itemId = resultSet.getInt("item_id");
                    int amount = resultSet.getInt("amount");

                    if (0 < amount) {
                        stock.add(new StockData(itemId, amount));
                    }
                }
            }
        }

        return stock;
    }

    @Override
    public void saveStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData, @NotNull Int2IntFunction itemIdRemapper) throws Exception {
        try (var connection = this.database.getConnection()) {
            var strUuid = uuid.toString();

            try (var statement = this.prepareStatement(connection, "DELETE FROM `%table%` WHERE `uuid`=?")) {
                statement.setString(1, strUuid);
                statement.execute();
            }

            try (var statement = this.prepareStatement(connection, "INSERT INTO `%table%` (`uuid`, `item_id`, `amount`) VALUES(?,?,?)")) {
                for (var data : stockData) {
                    statement.setString(1, strUuid);
                    statement.setInt(2, itemIdRemapper.applyAsInt(data.itemId()));
                    statement.setInt(3, data.amount());
                    statement.addBatch();
                }

                statement.executeBatch();
            }
        }
    }

    @Override
    public void savePartialStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData) throws Exception {
        try (var connection = this.database.getConnection()) {
            var strUuid = uuid.toString();

            try (var statement = this.prepareStatement(connection, insertOrUpdateStockDataStatement())) {
                for (var data : stockData) {
                    statement.setString(1, strUuid);
                    statement.setInt(2, data.itemId());
                    statement.setInt(3, data.amount());
                    statement.addBatch();
                }

                statement.executeBatch();
            }
        }
    }

    @Override
    public void cleanupZeroStockData() throws Exception {
        try (var connection = this.database.getConnection();
             var statement = prepareStatement(connection, "DELETE FROM `%table%` WHERE `amount`=?")) {
            statement.setInt(1, 0);
            statement.execute();
        }
    }

    @Override
    public boolean hasZeroStock() throws Exception {
        try (var connection = this.database.getConnection();
             var statement = prepareStatement(connection, "SELECT COUNT(*) FROM `%table%` WHERE `amount`=0");
             var result = statement.executeQuery()) {
            return result.next() && 0 < result.getInt(1);
        }
    }

    private @NotNull String insertOrUpdateStockDataStatement() {
        if (this.database instanceof MySQLDatabase) {
            return "INSERT INTO `%table%` (`uuid`, `item_id`, `amount`) VALUES (?, ?, ?) AS new ON DUPLICATE KEY UPDATE `amount` = new.amount";
        } else if (this.database instanceof SQLiteDatabase) {
            return "INSERT INTO `%table%` (`uuid`, `item_id`, `amount`) VALUES (?, ?, ?) ON CONFLICT (`uuid`, `item_id`) DO UPDATE SET `amount` = excluded.amount";
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
