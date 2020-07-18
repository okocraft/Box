package net.okocraft.box.plugin.database.connector;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A class that implements the connection with SQLite.
 * <p>
 * This class is copied from <a href="https://github.com/SiroPlugins/DatabaseLibs">DatabaseLibs</a>
 */
public class SQLite extends AbstractDatabase {

    /**
     * Create an instance by specifying the connection name and the database file.
     *
     * @param name   A connection name.
     * @param dbPath The path to the database file to connect to.
     */
    public SQLite(@NotNull String name, @NotNull Path dbPath) {
        this(name, dbPath, "org.sqlite.JDBC");
    }

    /**
     * Create an instance by specifying the connection name, the database file, and a driver class.
     *
     * @param name            A connection name.
     * @param dbPath          The path to the database file to connect to.
     * @param driverClassName Driver class name to use.
     */
    public SQLite(@NotNull String name, @NotNull Path dbPath, @NotNull String driverClassName) {
        this(name, "jdbc:sqlite:" + dbPath.toString(), driverClassName);
    }

    /**
     * Create an instance by specifying the connection name, the jdbc url, and a driver class.
     *
     * @param name            A connection name.
     * @param jdbcUrl         URL used for connection.
     * @param driverClassName Driver class name to use.
     */
    public SQLite(@NotNull String name, @NotNull String jdbcUrl, @NotNull String driverClassName) {
        super(name, jdbcUrl, driverClassName);
    }

    /**
     * This implementation does nothing.
     *
     * @param config A config to set
     */
    @Override
    protected void addProperties(@NotNull HikariConfig config) {
        // do nothing
    }

    /**
     * End connection pooling and disconnect from the database.
     * <p>
     * This implementation automatically releases free space before ending.
     *
     * @see SQLite#shutdown(boolean)
     */
    @Override
    public void shutdown() {
        shutdown(true);
    }

    /**
     * Returns the database type.
     *
     * @return {@link Database.Type#SQLITE}
     */
    @NotNull
    @Override
    public Type getType() {
        return Type.SQLITE;
    }

    /**
     * Specify whether to release the free space before ending, and end the connection pooling.
     *
     * @param vacuum True if free space is released, false otherwise.
     */
    public void shutdown(boolean vacuum) {
        if (vacuum) {
            vacuum();
        }

        if (hikari != null) {
            hikari.close();
            hikari = null;
        }
    }

    public void vacuum() {
        try (Connection c = getConnection(); Statement statement = c.createStatement()) {
            statement.execute("VACUUM");
        } catch (SQLException ignored) {
        }
    }
}
