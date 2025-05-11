package net.okocraft.box.storage.implementation.database.operator;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public abstract class CustomItemTableOperator {

    private final String createTableStatement;
    private final String insertStatement;
    private final String selectItemDataStatement;
    private final String updateItemDataStatement;

    public CustomItemTableOperator(@NotNull String tablePrefix) {
        var tableName = tablePrefix + "custom_items";

        this.createTableStatement = """
            CREATE TABLE IF NOT EXISTS `%s` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT,
              `data` VARCHAR(50) NOT NULL
            )
            """.formatted(tableName);

        this.insertStatement = "INSERT INTO `%s` (`id`, `data`) VALUES(?,?)".formatted(tableName);
        this.selectItemDataStatement = "SELECT `data` FROM `%s` WHERE `id`=?".formatted(tableName);
        this.updateItemDataStatement = "UPDATE `%s` SET `data`=? WHERE `id`=?".formatted(tableName);
    }

    public void initTable(@NotNull Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(this.createTableStatement);
        }
    }

    public void insert(@NotNull Connection connection, int id, byte[] data) throws SQLException {
        try (var statement = connection.prepareStatement(this.insertStatement)) {
            statement.setInt(1, id);
            this.writeBytes(statement, 2, data);
            statement.executeUpdate();
        }
    }

    public PreparedStatement insertStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.insertStatement);
    }

    public void addInsertBatch(@NotNull PreparedStatement statement, int id, byte[] data) throws SQLException {
        statement.setInt(1, id);
        this.writeBytes(statement, 2, data);
    }

    public @NotNull Optional<byte[]> selectItemData(@NotNull Connection connection, int id) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectItemDataStatement)) {
            statement.setInt(1, id);

            try (var resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(this.readBytes(resultSet, 1)) : Optional.empty();
            }
        }
    }

    public @NotNull PreparedStatement updateStatement(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement(this.updateItemDataStatement);
    }

    public void addUpdateBatch(@NotNull PreparedStatement statement, int id, byte[] data) throws SQLException {
        this.writeBytes(statement, 1, data);
        statement.setInt(2, id);
    }

    @SuppressWarnings("SameParameterValue")
    protected abstract byte[] readBytes(@NotNull ResultSet resultSet, int pos) throws SQLException;

    protected abstract void writeBytes(@NotNull PreparedStatement statement, int pos, byte[] data) throws SQLException;
}
