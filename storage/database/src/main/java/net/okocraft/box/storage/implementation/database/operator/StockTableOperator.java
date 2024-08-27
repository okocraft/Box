package net.okocraft.box.storage.implementation.database.operator;

import net.okocraft.box.api.model.stock.StockData;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class StockTableOperator {

    private final String createTableStatement;
    private final String createIndexStatement;
    private final String selectStockByUUIDStatement;
    private final String deleteStockByUUIDStatement;
    private final String insertStockStatement;
    private final String upsertStockStatement;
    private final String deleteZeroStockStatement;
    private final String countZeroStockStatement;
    private final String selectStockByItemIdStatement;
    private final String selectStockByUUIDAndItemIdStatement;
    private final String updateItemIdStatement;
    private final String updateAmountStatement;
    private final String deleteStockByItemIdsStatement;

    public StockTableOperator(@NotNull String tablePrefix) {
        var tableName = tablePrefix + "stock";

        this.createTableStatement = """
            CREATE TABLE IF NOT EXISTS `%s` (
              `uuid` VARCHAR(36) NOT NULL,
              `item_id` INTEGER  NOT NULL,
              `amount` INTEGER NOT NULL,
              PRIMARY KEY (`uuid`, `item_id`)
            )
            """.formatted(tableName);
        this.createIndexStatement = "CREATE INDEX IF NOT EXISTS `%1$s_amount` ON `%1$s` (`amount`)".formatted(tableName);

        this.selectStockByUUIDStatement = "SELECT `item_id`, `amount` FROM `%s` WHERE `uuid`=?".formatted(tableName);
        this.deleteStockByUUIDStatement = "DELETE FROM `%s` WHERE `uuid`=?".formatted(tableName);
        this.insertStockStatement = "INSERT INTO `%s` (`uuid`, `item_id`, `amount`) VALUES(?,?,?)".formatted(tableName);
        this.upsertStockStatement = this.upsertStockStatement(tableName);
        this.deleteZeroStockStatement = "DELETE FROM `%s` WHERE `amount`=0".formatted(tableName);
        this.countZeroStockStatement = "SELECT COUNT(`amount`) FROM `%s` WHERE `amount`=0".formatted(tableName);
        this.selectStockByItemIdStatement = "SELECT `uuid`, `amount` FROM `%s` WHERE `item_id` = ?".formatted(tableName);
        this.selectStockByUUIDAndItemIdStatement = "SELECT `amount` FROM `%s` WHERE `uuid` = ? AND `item_id` = ?".formatted(tableName);
        this.updateItemIdStatement = "UPDATE `%s` SET `item_id` = ? WHERE `uuid` = ? AND `item_id` = ?".formatted(tableName);
        this.updateAmountStatement = "UPDATE `%s` SET `amount` = ? WHERE `uuid` = ? AND `item_id` = ?".formatted(tableName);
        this.deleteStockByItemIdsStatement = "DELETE FROM `%s` WHERE `item_id` IN (:ITEM_IDS:)".formatted(tableName);
    }

    public void initTable(@NotNull Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(this.createTableStatement);
            statement.execute(this.createIndexStatement);
        }
    }

    public void selectStockByUUID(@NotNull Connection connection, @NotNull UUID uuid, @NotNull Consumer<StockData> consumer) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectStockByUUIDStatement)) {
            statement.setString(1, uuid.toString());
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

    public void deleteStockByUUID(@NotNull Connection connection, @NotNull String uuid) throws SQLException {
        try (var statement = connection.prepareStatement(this.deleteStockByUUIDStatement)) {
            statement.setString(1, uuid);
            statement.execute();
        }
    }

    public @NotNull PreparedStatement insertStockStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.insertStockStatement);
    }

    public void addInsertStockBatch(@NotNull PreparedStatement statement, @NotNull String uuid, int itemId, int amount) throws SQLException {
        statement.setString(1, uuid);
        statement.setInt(2, itemId);
        statement.setInt(3, amount);
        statement.addBatch();
    }

    public @NotNull PreparedStatement upsertStockStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.upsertStockStatement);
    }

    public void addUpsertStockBatch(@NotNull PreparedStatement statement, @NotNull String uuid, int itemId, int amount) throws SQLException {
        statement.setString(1, uuid);
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

    public void selectStockByItemId(@NotNull PreparedStatement statement, int itemId, @NotNull BiConsumer<String, Integer> consumer) throws SQLException {
        statement.setInt(1, itemId);

        try (var resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                consumer.accept(resultSet.getString(1), resultSet.getInt(2));
            }
        }
    }

    public @NotNull PreparedStatement selectStockByUUIDAndItemIdStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.selectStockByUUIDAndItemIdStatement);
    }

    public void selectStockByUUIDAndItemId(@NotNull PreparedStatement statement, @NotNull String uuid, int itemId, @NotNull IntConsumer consumer, @NotNull Runnable onNotFound) throws SQLException {
        statement.setString(1, uuid);
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

    public void addUpdateItemIdBatch(@NotNull PreparedStatement statement, @NotNull String uuid, int oldItemId, int newItemId) throws SQLException {
        statement.setInt(1, newItemId);
        statement.setString(2, uuid);
        statement.setInt(3, oldItemId);
        statement.addBatch();
    }

    public @NotNull PreparedStatement updateAmountStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.updateAmountStatement);
    }

    public void addUpdateAmountBatch(@NotNull PreparedStatement statement, @NotNull String uuid, int itemId, int amount) throws SQLException {
        statement.setInt(1, amount);
        statement.setString(2, uuid);
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

}
