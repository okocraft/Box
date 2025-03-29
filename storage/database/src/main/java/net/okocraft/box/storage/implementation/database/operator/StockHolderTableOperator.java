package net.okocraft.box.storage.implementation.database.operator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

@NotNullByDefault
public class StockHolderTableOperator {

    private final String createTableStatement;
    private final String selectStockHolderIdByUUID;
    private final String insertStockHolderIdByUUID;

    public StockHolderTableOperator(String tablePrefix, String uuidType) {
        var tableName = tablePrefix + "stockholders";
        this.createTableStatement = """
            CREATE TABLE IF NOT EXISTS `%s` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT,
            `uuid` %s NOT NULL UNIQUE
            )
            """.formatted(tableName, uuidType);
        this.selectStockHolderIdByUUID = "SELECT id FROM `%s` WHERE `uuid`=?".formatted(tableName);
        this.insertStockHolderIdByUUID = "INSERT INTO `%s` (`uuid`) VALUES (?)".formatted(tableName);
    }

    public void initTable(@NotNull Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(this.createTableStatement);
        }
    }

    public int getStockHolderIdByUUID(Connection connection, UUID uuid) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectStockHolderIdByUUID)) {
            statement.setBytes(1, UUIDConverters.toBytes(uuid));
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }

        try (var statement = connection.prepareStatement(this.insertStockHolderIdByUUID)) {
            statement.setBytes(1, UUIDConverters.toBytes(uuid));
            statement.executeUpdate();
            try (var resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }

        throw new SQLException("Cannot create stock holder id for uuid " + uuid);
    }
}
