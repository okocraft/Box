package net.okocraft.box.storage.implementation.database.operator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public abstract class MetaTableOperator {

    private final String tableName;
    private final String createTableStatement;
    private final String selectValueStatement;
    private final String upsertValueStatement;

    public MetaTableOperator(@NotNull String tablePrefix) {
        this.tableName = tablePrefix + "meta";

        this.createTableStatement = """
            CREATE TABLE IF NOT EXISTS `%s` (
              `key` VARCHAR(25) PRIMARY KEY NOT NULL,
              `value` VARCHAR(16) NOT NULL
            )
            """.formatted(this.tableName);

        this.selectValueStatement = "SELECT `value` FROM `%s` WHERE `key`=?".formatted(this.tableName);
        this.upsertValueStatement = this.upsertStatement(this.tableName);
    }

    public void initTable(@NotNull Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(this.createTableStatement);
        }
    }

    public boolean existsTable(@NotNull Connection connection) throws SQLException {
            DatabaseMetaData meta = connection.getMetaData();

            try (var tables = meta.getTables(null, null, this.tableName.toUpperCase(), new String[] { "TABLE" })) {
                return tables.next();
            }
    }

    public @Nullable String selectValue(@NotNull Connection connection, @NotNull String key) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectValueStatement)) {
            statement.setString(1, key);

            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("value");
                }
            }
        }
        return null;
    }

    public @Nullable Integer selectValueAsIntOrNull(@NotNull Connection connection, @NotNull String key) throws SQLException {
        var result = this.selectValue(connection, key);
        return result != null ? Integer.valueOf(result) : null;
    }

    public void upsertValue(@NotNull Connection connection, @NotNull String key, @NotNull String value) throws SQLException {
        try (var statement = connection.prepareStatement(this.upsertValueStatement)) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.executeUpdate();
        }
    }

    protected abstract @NotNull String upsertStatement(@NotNull String tableName);
}
