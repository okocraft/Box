package net.okocraft.box.storage.implementation.database.operator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.BiConsumer;

public abstract class UserTableOperator {

    private final String createTableStatement;
    private final String createIndexStatement;
    private final String selectNameByUUIDStatement;
    private final String selectUUIDByNameStatement;
    private final String selectAllStatement;
    private final String upsertStatement;

    public UserTableOperator(@NotNull String tablePrefix, @NotNull String uuidType) {
        var tableName = tablePrefix + "users";

        this.createTableStatement = """
            CREATE TABLE IF NOT EXISTS `%s` (
              `uuid` %s PRIMARY KEY NOT NULL,
              `username` VARCHAR(16) NOT NULL
            )
            """.formatted(tableName, uuidType);
        this.createIndexStatement = "CREATE INDEX IF NOT EXISTS `%1$s_username` ON `%1$s` (`username`)".formatted(tableName);

        this.selectNameByUUIDStatement = "SELECT `username` FROM `%s` WHERE `uuid`=?".formatted(tableName);
        this.selectUUIDByNameStatement = "SELECT `uuid` FROM `%s` WHERE `username` LIKE ?".formatted(tableName);
        this.selectAllStatement = "SELECT * FROM `%s`".formatted(tableName);
        this.upsertStatement = this.upsertStatement(tableName);
    }

    public void initTable(@NotNull Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(this.createTableStatement);
            statement.execute(this.createIndexStatement);
        }
    }

    public @Nullable String selectUsernameByUUID(@NotNull Connection connection, @NotNull UUID uuid) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectNameByUUIDStatement)) {
            statement.setBytes(1, UUIDConverters.toBytes(uuid));
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("username");
                }
            }
        }
        return null;
    }

    public @Nullable UUID selectUUIDByUserName(@NotNull Connection connection, @NotNull String username) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectUUIDByNameStatement)) {
            statement.setString(1, username);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return UUIDConverters.fromBytes(resultSet.getBytes("uuid"));
                }
            }
        }
        return null;
    }

    public void selectAllUsers(@NotNull Connection connection, @NotNull BiConsumer<UUID, String> consumer) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectAllStatement)) {
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    UUID uuid = UUIDConverters.fromBytes(resultSet.getBytes("uuid"));
                    consumer.accept(uuid, resultSet.getString("username"));
                }
            }
        }
    }

    public void upsertUser(@NotNull Connection connection, @NotNull UUID uuid, @NotNull String username) throws SQLException {
        try (var statement = connection.prepareStatement(this.upsertStatement)) {
            statement.setBytes(1, UUIDConverters.toBytes(uuid));
            statement.setString(2, username);
            statement.executeUpdate();
        }
    }

    public @NotNull PreparedStatement upsertUserStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.upsertStatement);
    }

    public void addUpsertUserBatch(@NotNull PreparedStatement statement, @NotNull UUID uuid, @NotNull String username) throws SQLException {
        statement.setBytes(1, UUIDConverters.toBytes(uuid));
        statement.setString(2, username);
        statement.addBatch();
    }

    protected abstract @NotNull String upsertStatement(@NotNull String tableName);

}
