package net.okocraft.box.storage.implementation.database.operator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;

public abstract class CustomDataTableOperator {

    private final String tableName;
    private final String createTableStatement;
    private final String selectDataByKeyStatement;
    private final String selectDataByNamespaceStatement;
    private final String selectAllDataStatement;
    private final String deleteDataStatement;
    private final String upsertDataStatement;

    public CustomDataTableOperator(@NotNull String prefix, @NotNull String suffix) {
        this.tableName = prefix + "custom_data" + suffix;

        this.createTableStatement = """
            CREATE TABLE IF NOT EXISTS `%s` (
              `key` VARCHAR(50) PRIMARY KEY NOT NULL,
              `data` BLOB  NOT NULL
            )
            """.formatted(this.tableName);

        this.selectDataByKeyStatement = "SELECT `data` FROM `%s` WHERE `key`=?".formatted(this.tableName);
        this.selectDataByNamespaceStatement = "SELECT `key`, `data` FROM `%s` WHERE `key` LIKE ?".formatted(this.tableName);
        this.selectAllDataStatement = "SELECT `key`, `data` FROM `%s`".formatted(this.tableName);
        this.deleteDataStatement = "DELETE FROM `%s` WHERE `key`=?".formatted(this.tableName);
        this.upsertDataStatement = this.upsertStatement(this.tableName);
    }

    public @NotNull String tableName() {
        return this.tableName;
    }

    public void initTable(@NotNull Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(this.createTableStatement);
        }
    }

    public byte @Nullable [] selectDataByKey(@NotNull Connection connection, @NotNull String key) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectDataByKeyStatement)) {
            statement.setString(1, key);

            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return this.readBytes(resultSet, 1);
                }
            }
        }

        return null;
    }

    public void selectDataByNamespace(@NotNull Connection connection, @NotNull String namespace, @NotNull BiConsumer<String, byte[]> consumer) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectDataByNamespaceStatement)) {
            statement.setString(1, namespace + ":%");

            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    consumer.accept(resultSet.getString(1), this.readBytes(resultSet, 2));
                }
            }
        }
    }

    public void selectAllData(@NotNull Connection connection, @NotNull BiConsumer<String, byte[]> consumer) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectAllDataStatement)) {
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    consumer.accept(resultSet.getString(1), this.readBytes(resultSet, 2));
                }
            }
        }
    }

    public void deleteData(@NotNull Connection connection, @NotNull String key) throws SQLException {
        try (var statement = connection.prepareStatement(this.deleteDataStatement)) {
            statement.setString(1, key);
            statement.executeUpdate();
        }
    }

    public void upsertData(@NotNull Connection connection, @NotNull String key, byte @NotNull [] data) throws SQLException {
        try (var statement = connection.prepareStatement(this.upsertDataStatement)) {
            statement.setString(1, key);
            this.writeBytes(statement, 2, data);
            statement.executeUpdate();
        }
    }

    protected abstract @NotNull String upsertStatement(@NotNull String tableName);

    protected abstract byte[] readBytes(@NotNull ResultSet resultSet, int pos) throws SQLException;

    @SuppressWarnings("SameParameterValue")
    protected abstract void writeBytes(@NotNull PreparedStatement statement, int pos, byte[] data) throws SQLException;

}
