package net.okocraft.box.plugin.database.table;

import net.okocraft.box.plugin.database.connector.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public abstract class AbstractTable {

    protected final Database database;
    protected final String tableName;

    public AbstractTable(@NotNull Database database, @NotNull String tableName) {
        this.database = database;
        this.tableName = tableName;

        try {
            createTable();
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    protected abstract void createTable() throws SQLException;

    @NotNull
    protected String replaceTableName(@NotNull String sql) {
        return sql.replace("%table%", tableName);
    }

    @NotNull
    protected String getAutoIncrementSQL() {
        return database.getType() == Database.Type.SQLITE ? "AUTOINCREMENT" : "AUTO_INCREMENT";
    }
}
