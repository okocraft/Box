package net.okocraft.box.storage.implementation.database.operator;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

@NotNullByDefault
public class StockHolderTableOperator {

    private final String createTableStatement;
    private final String selectStockHolderIdByUUID;
    private final String insertStockHolderIdByUUID;
    private final String selectAllStockHolderIDs;

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
        this.selectAllStockHolderIDs = "SELECT id, uuid FROM `%s`;".formatted(tableName);
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

    public Int2ObjectMap<UUID> getAllUUIDByStockHolderId(Connection connection) throws SQLException {
        var result = new Int2ObjectOpenHashMap<UUID>();
        try (var statement = connection.prepareStatement(this.selectAllStockHolderIDs)) {
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.put(resultSet.getInt(1), UUIDConverters.fromBytes(resultSet.getBytes(2)));
                }
            }
        }
        return result;
    }

    public void insertStockHolderUUIDs(Connection connection, Collection<UUID> uuids) throws SQLException {
        try (var statement = connection.prepareStatement(this.insertStockHolderIdByUUID)) {
            for (var uuid : uuids) {
                statement.setBytes(1, UUIDConverters.toBytes(uuid));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public Object2IntMap<UUID> getAllStockHolderIdByUUID(Connection connection) throws SQLException {
        var result = new Object2IntOpenHashMap<UUID>();
        try (var statement = connection.prepareStatement(this.selectAllStockHolderIDs)) {
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.put(UUIDConverters.fromBytes(resultSet.getBytes(2)), resultSet.getInt(1));
                }
            }
        }
        return result;
    }
}
