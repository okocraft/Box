package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.schema.TableSchema;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractTable {

    protected final Database database;
    private final TableSchema tableSchema;

    protected AbstractTable(@NotNull Database database, @NotNull TableSchema tableSchema) {
        this.database = database;
        this.tableSchema = tableSchema;
    }

    protected void createTableAndIndex() throws SQLException {
        try (var connection = this.database.getConnection();
             var statement = connection.createStatement()) {
            statement.addBatch(this.replaceTableName(tableSchema.createTableStatement()));

            for (var indexStatement : this.tableSchema.createIndexStatements()) {
                statement.addBatch(this.replaceTableName(indexStatement));
            }

            statement.executeBatch();
        }
    }

    protected @NotNull String replaceTableName(@NotNull String sql) {
        return sql.replace("%table%", tableSchema.tableName());
    }

    protected @NotNull PreparedStatement prepareStatement(@NotNull Connection connection, @NotNull String sql) throws SQLException {
        var tableNameReplaced = replaceTableName(sql);
        return connection.prepareStatement(tableNameReplaced);
    }

    protected void writeBytesToStatement(@NotNull PreparedStatement statement, int pos, byte[] bytes) throws SQLException {
        if (database.getType() == Database.Type.SQLITE) {
            statement.setBytes(pos, bytes);
        } else {
            var blob = statement.getConnection().createBlob();
            blob.setBytes(1, bytes);
            statement.setBlob(pos, blob);
        }
    }

    protected byte[] readBytesFromResultSet(@NotNull ResultSet resultSet, @NotNull String columName) throws SQLException {
        if (database.getType() == Database.Type.SQLITE) {
            return resultSet.getBytes(columName);
        } else {
            var blob = resultSet.getBlob(columName);
            return blob.getBytes(1, (int) blob.length());
        }
    }
}
