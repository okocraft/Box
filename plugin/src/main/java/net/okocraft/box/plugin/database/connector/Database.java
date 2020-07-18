package net.okocraft.box.plugin.database.connector;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * An interface that manages the connection with the database.
 * <p>
 * This interface is copied from <a href="https://github.com/SiroPlugins/DatabaseLibs">DatabaseLibs</a>
 */
public interface Database {

    static @NotNull Database connectMySQL(@NotNull String address, @NotNull String databaseName,
                                          @NotNull String username, @NotNull String password, boolean isUsingSSL) {
        String jdbc = MySQL.getUrl(address, databaseName, isUsingSSL);

        return new MySQL("Box-Database", jdbc, username, password);
    }

    static @NotNull Database connectSQLite(@NotNull Path dbPath) {
        if (Files.isDirectory(dbPath)) {
            throw new IllegalArgumentException("dbPath must be a file path");
        }

        if (!Files.exists(dbPath)) {
            try {
                Files.createDirectories(dbPath.getParent());
                Files.createFile(dbPath);
            } catch (IOException e) {
                throw new IllegalStateException("Could not create a file: " + dbPath.toAbsolutePath().toString(), e);
            }
        }

        return new SQLite("Box-Database", dbPath);
    }

    /**
     * Start connection pooling and connect to the database.
     */
    void start();

    /**
     * Gets a {@link Connection} to the database.
     *
     * @return database connection
     * @throws SQLException If an exception occurs while establishing a connection with the database.
     */
    @NotNull
    Connection getConnection() throws SQLException;

    /**
     * End connection pooling and disconnect from the database.
     */
    void shutdown();

    /**
     * Add connection setting.
     *
     * @param name  a property
     * @param value a setting value
     * @see com.zaxxer.hikari.HikariConfig#addDataSourceProperty(String, Object)
     */
    void addDataSourceProperty(@NotNull String name, @NotNull Object value);

    /**
     * Returns the database type.
     *
     * @return a database type
     */
    @NotNull
    Type getType();

    /**
     * List of implemented database types.
     */
    enum Type {
        MYSQL,
        SQLITE
    }
}