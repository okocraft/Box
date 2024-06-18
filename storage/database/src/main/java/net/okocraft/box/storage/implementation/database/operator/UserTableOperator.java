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

    public UserTableOperator(@NotNull String tablePrefix) {
        var tableName = tablePrefix + "users";

        this.createTableStatement = """
                CREATE TABLE IF NOT EXISTS `%s` (
                  `uuid` VARCHAR(36) PRIMARY KEY NOT NULL,
                  `username` VARCHAR(16) NOT NULL
                )
                """.formatted(tableName);
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
            statement.setString(1, uuid.toString());
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("username");
                }
            }
        }
        return null;
    }

    public @Nullable String selectUUIDByUserName(@NotNull Connection connection, @NotNull String username) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectUUIDByNameStatement)) {
            statement.setString(1, username);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("uuid");
                }
            }
        }
        return null;
    }

    public void selectAllUsers(@NotNull Connection connection, @NotNull BiConsumer<String, String> consumer) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectAllStatement)) {
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    consumer.accept(resultSet.getString("uuid"), resultSet.getString("username"));
                }
            }
        }
    }

    public void upsertUser(@NotNull Connection connection, @NotNull UUID uuid, @NotNull String username) throws SQLException {
        try (var statement = connection.prepareStatement(this.upsertStatement)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, username);
            statement.executeUpdate();
        }
    }

    public @NotNull PreparedStatement upsertUserStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.upsertStatement);
    }

    public void addUpsertUserBatch(@NotNull PreparedStatement statement, @NotNull UUID uuid, @NotNull String username) throws SQLException {
        statement.setString(1, uuid.toString());
        statement.setString(2, username);
        statement.addBatch();
    }

    protected abstract @NotNull String upsertStatement(@NotNull String tableName);

}
