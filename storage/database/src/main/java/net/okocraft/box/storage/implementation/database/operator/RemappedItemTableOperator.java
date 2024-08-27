package net.okocraft.box.storage.implementation.database.operator;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RemappedItemTableOperator {

    private final String createTableStatement;
    private final String insertStatement;
    private final String selectAllRemappedIdStatement;
    private final String selectAllRemappedIdByVersionStatement;

    public RemappedItemTableOperator(@NotNull String tablePrefix) {
        var tableName = tablePrefix + "remapped_items";

        this.createTableStatement = """
            CREATE TABLE IF NOT EXISTS `%s` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT,
              `name` VARCHAR(50) NOT NULL,
              `remapped_to` INTEGER NOT NULL,
              `in_version` INTEGER NOT NULL
            )
            """.formatted(tableName);

        this.insertStatement = "INSERT INTO `%s` (`id`, `name`, `remapped_to`, `in_version`) VALUES(?,?,?,?)".formatted(tableName);
        this.selectAllRemappedIdStatement = "SELECT `id`, `remapped_to`, `in_version` FROM `%s`".formatted(tableName);
        this.selectAllRemappedIdByVersionStatement = this.selectAllRemappedIdStatement + " WHERE `in_version`=?";
    }

    public void initTable(@NotNull Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(this.createTableStatement);
        }
    }

    public void insert(@NotNull Connection connection, int id, @NotNull String name, int remappedTo, int inVersion) throws SQLException {
        try (var statement = connection.prepareStatement(this.insertStatement)) {
            statement.setInt(1, id);
            statement.setString(2, name);
            statement.setInt(3, remappedTo);
            statement.setInt(4, inVersion);
            statement.executeUpdate();
        }
    }

    public void selectAllRemappedIds(@NotNull Connection connection, @NotNull BiConsumer<Integer, RemappedItem> remappedIdConsumer) throws SQLException {
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(this.selectAllRemappedIdStatement)) {
            while (resultSet.next()) {
                remappedIdConsumer.accept(resultSet.getInt(3), new RemappedItem(resultSet.getInt(1), resultSet.getInt(2)));
            }
        }
    }

    public void selectAllRemappedIdsByVersion(@NotNull Connection connection, int inVersion, @NotNull Consumer<RemappedItem> remappedIdConsumer) throws SQLException {
        try (var statement = connection.prepareStatement(this.selectAllRemappedIdByVersionStatement)) {
            statement.setInt(1, inVersion);

            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    remappedIdConsumer.accept(new RemappedItem(resultSet.getInt(1), resultSet.getInt(2)));
                }
            }
        }
    }

    public record RemappedItem(int oldId, int newId) {
    }
}
