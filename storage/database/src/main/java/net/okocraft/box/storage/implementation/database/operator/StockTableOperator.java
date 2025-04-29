package net.okocraft.box.storage.implementation.database.operator;

import net.okocraft.box.api.model.stock.StockData;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class StockTableOperator {

    private final String createTableStatement;
    private final String createIndexStatement;
    private final String selectStockByIdStatement;
    private final String deleteStockByIdStatement;
    private final String insertStockStatement;
    private final String upsertStockStatement;
    private final String deleteZeroStockStatement;
    private final String countZeroStockStatement;
    private final String selectStockByItemIdStatement;
    private final String selectStockByIdAndItemIdStatement;
    private final String updateItemIdStatement;
    private final String updateAmountStatement;
    private final String deleteStockByItemIdsStatement;
    private final String selectAllStockStatement;

    public StockTableOperator(@NotNull String tablePrefix) {
        var tableName = tablePrefix + "stock";

        this.createTableStatement = """
            CREATE TABLE IF NOT EXISTS `%s` (
              `stock_id` INTEGER NOT NULL,
              `item_id` INTEGER  NOT NULL,
              `amount` INTEGER NOT NULL,
              PRIMARY KEY (`stock_id`, `item_id`)
            )
            """.formatted(tableName);
        this.createIndexStatement = "CREATE INDEX IF NOT EXISTS `%1$s_amount` ON `%1$s` (`amount`)".formatted(tableName);

        this.selectStockByIdStatement = "SELECT `item_id`, `amount` FROM `%s` WHERE `stock_id`=?".formatted(tableName);
        this.deleteStockByIdStatement = "DELETE FROM `%s` WHERE `stock_id`=?".formatted(tableName);
        this.insertStockStatement = "INSERT INTO `%s` (`stock_id`, `item_id`, `amount`) VALUES(?,?,?)".formatted(tableName);
        this.upsertStockStatement = this.upsertStockStatement(tableName);
        this.deleteZeroStockStatement = "DELETE FROM `%s` WHERE `amount`=0".formatted(tableName);
        this.countZeroStockStatement = "SELECT COUNT(`amount`) FROM `%s` WHERE `amount`=0".formatted(tableName);
        this.selectStockByItemIdStatement = "SELECT `stock_id`, `amount` FROM `%s` WHERE `item_id` = ?".formatted(tableName);
        this.selectStockByIdAndItemIdStatement = "SELECT `amount` FROM `%s` WHERE `stock_id` = ? AND `item_id` = ?".formatted(tableName);
        this.updateItemIdStatement = "UPDATE `%s` SET `item_id` = ? WHERE `stock_id` = ? AND `item_id` = ?".formatted(tableName);
        this.updateAmountStatement = "UPDATE `%s` SET `amount` = ? WHERE `stock_id` = ? AND `item_id` = ?".formatted(tableName);
        this.deleteStockByItemIdsStatement = "DELETE FROM `%s` WHERE `item_id` IN (:ITEM_IDS:)".formatted(tableName);
        this.selectAllStockStatement = "SELECT stock_id, item_id, amount FROM `%s`".formatted(tableName);
    }

    public void initTable(@NotNull Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(this.createTableStatement);
            statement.execute(this.createIndexStatement);
        }
    }

    public void selectStockById(@NotNull Connection connection, int stockId, @NotNull Consumer<StockData> consumer) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectStockByIdStatement)) {
            statement.setInt(1, stockId);
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int itemId = resultSet.getInt("item_id");
                    int amount = resultSet.getInt("amount");

                    if (0 < amount) {
                        consumer.accept(new StockData(itemId, amount));
                    }
                }
            }
        }
    }

    public void deleteStockByStockId(@NotNull Connection connection, int stockId) throws SQLException {
        try (var statement = connection.prepareStatement(this.deleteStockByIdStatement)) {
            statement.setInt(1, stockId);
            statement.execute();
        }
    }

    public @NotNull PreparedStatement insertStockStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.insertStockStatement);
    }

    public void addInsertStockBatch(@NotNull PreparedStatement statement, int stockId, int itemId, int amount) throws SQLException {
        statement.setInt(1, stockId);
        statement.setInt(2, itemId);
        statement.setInt(3, amount);
        statement.addBatch();
    }

    public @NotNull PreparedStatement upsertStockStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.upsertStockStatement);
    }

    public void addUpsertStockBatch(@NotNull PreparedStatement statement, int stockId, int itemId, int amount) throws SQLException {
        statement.setInt(1, stockId);
        statement.setInt(2, itemId);
        statement.setInt(3, amount);
        statement.addBatch();
    }

    public void deleteZeroStock(@NotNull Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(this.deleteZeroStockStatement);
        }
    }

    public int countZeroStock(@NotNull Connection connection) throws SQLException {
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(this.countZeroStockStatement)) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    public @NotNull PreparedStatement selectStockByItemIdStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.selectStockByItemIdStatement);
    }

    public void selectStockByItemId(@NotNull PreparedStatement statement, int itemId, @NotNull BiConsumer<Integer, Integer> consumer) throws SQLException {
        statement.setInt(1, itemId);

        try (var resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                consumer.accept(resultSet.getInt(1), resultSet.getInt(2));
            }
        }
    }

    public @NotNull PreparedStatement selectStockByIdAndItemIdStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.selectStockByIdAndItemIdStatement);
    }

    public void selectStockByIdAndItemId(@NotNull PreparedStatement statement, int stockId, int itemId, @NotNull IntConsumer consumer, @NotNull Runnable onNotFound) throws SQLException {
        statement.setInt(1, stockId);
        statement.setInt(2, itemId);

        try (var resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                consumer.accept(resultSet.getInt(1));
            } else {
                onNotFound.run();
            }
        }
    }

    public @NotNull PreparedStatement updateItemIdStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.updateItemIdStatement);
    }

    public void addUpdateItemIdBatch(@NotNull PreparedStatement statement, int stockId, int oldItemId, int newItemId) throws SQLException {
        statement.setInt(1, newItemId);
        statement.setInt(2, stockId);
        statement.setInt(3, oldItemId);
        statement.addBatch();
    }

    public @NotNull PreparedStatement updateAmountStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.updateAmountStatement);
    }

    public void addUpdateAmountBatch(@NotNull PreparedStatement statement, int stockId, int itemId, int amount) throws SQLException {
        statement.setInt(1, amount);
        statement.setInt(2, stockId);
        statement.setInt(3, itemId);
        statement.addBatch();
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    public void deleteStockByItemIds(@NotNull Connection connection, @NotNull IntStream stream) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(this.deleteStockByItemIdsStatement.replace(":ITEM_IDS:", stream.mapToObj(Integer::toString).collect(Collectors.joining(", "))));
        }
    }

    protected abstract @NotNull String upsertStockStatement(@NotNull String tableName);

    public void selectAllStock(@NotNull Connection connection, @NotNull BiConsumer<Integer, StockData> consumer) throws SQLException{
        try (var statement = connection.prepareStatement(this.selectAllStockStatement)) {
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int stockId = resultSet.getInt(1);
                    StockData stockData = new StockData(resultSet.getInt(2), resultSet.getInt(3));
                    consumer.accept(stockId, stockData);
                }
            }
        }
    }

}
