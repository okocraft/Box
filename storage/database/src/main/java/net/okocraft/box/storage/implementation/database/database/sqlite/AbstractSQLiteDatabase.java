package net.okocraft.box.storage.implementation.database.database.sqlite;

import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.operator.OperatorProvider;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

abstract class AbstractSQLiteDatabase implements Database {

    protected final String tablePrefix;
    private final OperatorProvider operators;
    private NonCloseableConnection connection;

    protected AbstractSQLiteDatabase(@NotNull String tablePrefix) {
        this.tablePrefix = tablePrefix;
        this.operators = SQLiteOperators.create(this.tablePrefix);
    }

    @Override
    public String tablePrefix() {
        return this.tablePrefix;
    }

    @Override
    public @NotNull OperatorProvider operators() {
        return this.operators;
    }

    @Override
    public @NotNull Connection getConnection() throws SQLException {
        if (this.connection == null) {
            throw new IllegalStateException("This database is not initialized.");
        }

        return this.connection;
    }

    protected abstract @NotNull Connection createConnection() throws Exception;

    protected void connect() throws Exception {
        this.connection = new NonCloseableConnection(this.createConnection());
    }

    protected void disconnect() throws Exception {
        this.connection.shutdown();
    }

    protected static @NotNull Connection newConnection(@NotNull String filepath, @NotNull String filename) throws ReflectiveOperationException {
        return (Connection) Class.forName("org.sqlite.jdbc4.JDBC4Connection")
            .getConstructor(String.class, String.class, Properties.class)
            .newInstance("jdbc:sqlite:" + filepath, filename, new Properties());
    }
}
