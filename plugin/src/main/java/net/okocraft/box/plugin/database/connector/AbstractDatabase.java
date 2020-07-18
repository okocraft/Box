package net.okocraft.box.plugin.database.connector;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * This is an abstract class that partially implements {@link Database}.
 * <p>
 * This class is copied from <a href="https://github.com/SiroPlugins/DatabaseLibs">DatabaseLibs</a>
 */
public abstract class AbstractDatabase implements Database {

    protected final HikariConfig config;
    protected HikariDataSource hikari;

    /**
     * Specify the connection name, JDBC URL, and driver class.
     *
     * @param name            A connection name.
     * @param jdbcUrl         URL used for connection.
     * @param driverClassName Driver class name to use.
     */
    public AbstractDatabase(@NotNull String name, @NotNull String jdbcUrl, @NotNull String driverClassName) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        Objects.requireNonNull(driverClassName, "driverClassName");

        HikariConfig config = new HikariConfig();

        config.setPoolName(name);
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName(driverClassName);

        addProperties(config);

        this.config = config;
    }

    /**
     * Setting addition method that is called when the instance is created.
     *
     * @param config A config to set
     */
    protected abstract void addProperties(@NotNull HikariConfig config);

    /**
     * Start connection pooling and connect to the database.
     */
    @Override
    public void start() {
        hikari = new HikariDataSource(config);
    }

    /**
     * Gets a {@link Connection} to the database.
     *
     * @return database connection
     * @throws SQLException If an exception occurs while establishing a connection with the database.
     */
    @NotNull
    @Override
    public Connection getConnection() throws SQLException {
        if (this.hikari == null) {
            throw new SQLException("Could not connect to database. (hikari is null)");
        }

        Connection connection = this.hikari.getConnection();

        if (connection == null) {
            throw new SQLException("Could not connect to database.  (getConnection returned null)");
        }

        return connection;
    }

    /**
     * End connection pooling and disconnect from the database.
     */
    @Override
    public void shutdown() {
        if (hikari != null) {
            hikari.close();
            hikari = null;
        }
    }

    /**
     * Add connection setting.
     *
     * @param name  a property
     * @param value a setting value
     * @see HikariConfig#addDataSourceProperty(String, Object)
     */
    @Override
    public void addDataSourceProperty(@NotNull String name, @NotNull Object value) {
        config.addDataSourceProperty(name, value);
    }
}
