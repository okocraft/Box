package net.okocraft.box.storage.implementation.database.table;

import com.github.siroshun09.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.database.mysql.MySQLDatabase;
import net.okocraft.box.storage.implementation.database.database.sqlite.SQLiteDatabase;
import net.okocraft.box.storage.implementation.database.schema.TableSchema;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.function.BiConsumer;

public abstract class AbstractCustomDataTable extends AbstractTable implements CustomDataStorage {

    public AbstractCustomDataTable(@NotNull Database database, @NotNull TableSchema schema) {
        super(database, schema);
    }

    @Override
    public @NotNull MapNode loadData(@NotNull Key key) throws Exception {
        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT `data` FROM `%table%` WHERE `key`=? LIMIT 1")) {
            statement.setString(1, key.asString());

            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return this.readDataFromResultSet(resultSet);
                }
            }
        }

        return MapNode.create();
    }

    @Override
    public void saveData(@NotNull Key key, @NotNull MapNode mapNode) throws Exception {
        if (mapNode.value().isEmpty()) {
            try (var connection = database.getConnection();
                 var statement = prepareStatement(connection, "DELETE FROM `%table%` WHERE `key`=?")) {
                statement.setString(1, key.asString());
                statement.execute();
            }
        } else {
            try (var connection = database.getConnection();
                 var statement = prepareStatement(connection, this.insertOrUpdateDataStatement())) {
                statement.setString(1, key.asString());
                this.writeBytesToStatement(statement, 2, this.toBytes(mapNode));
                statement.execute();
            }
        }
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public void visitData(@NotNull String namespace, @NotNull BiConsumer<Key, MapNode> consumer) throws Exception {
        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT `key`, `data` FROM `%table%` WHERE `key` LIKE ?")) {
            statement.setString(1, namespace + ":%");

            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    consumer.accept(Key.key(resultSet.getString("key")), readDataFromResultSet(resultSet));
                }
            }
        }
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public void visitAllData(@NotNull BiConsumer<Key, MapNode> consumer) throws Exception {
        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT `key`, `data` FROM `%table%`")) {
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    consumer.accept(Key.key(resultSet.getString("key")), readDataFromResultSet(resultSet));
                }
            }
        }
    }

    private @NotNull String insertOrUpdateDataStatement() {
        if (database instanceof MySQLDatabase) {
            return "INSERT INTO `%table%` (`key`, `data`) VALUES (?, ?) AS new ON DUPLICATE KEY UPDATE `data` = new.data";
        } else if (database instanceof SQLiteDatabase) {
            return "INSERT INTO `%table%` (`key`, `data`) VALUES (?, ?) ON CONFLICT (`key`) DO UPDATE SET `data` = excluded.data";
        } else {
            throw new UnsupportedOperationException();
        }
    }

    protected abstract byte @NotNull [] toBytes(@NotNull MapNode node) throws Exception;

    protected abstract @NotNull MapNode readDataFromResultSet(@NotNull ResultSet resultSet) throws Exception;

}
