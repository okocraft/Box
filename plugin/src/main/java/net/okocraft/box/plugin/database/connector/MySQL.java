package net.okocraft.box.plugin.database.connector;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class that implements the connection with MySQL.
 * <p>
 * This class is copied from <a href=https://github.com/SiroPlugins/DatabaseLibs">DatabaseLibs</a>
 */
public class MySQL extends AbstractDatabase {

    /**
     * Create an instance by specifying the connection name, the jdbc url, the username and the password.
     *
     * @param name     A connection name.
     * @param jdbcUrl  URL used for connection.
     * @param username Username to use when connecting.
     * @param password Password to use when connecting.
     */
    public MySQL(@NotNull String name, @NotNull String jdbcUrl, @NotNull String username, @NotNull String password) {
        this(name, jdbcUrl, username, password, "com.mysql.jdbc.Driver");
    }

    /**
     * Create an instance by specifying the connection name, the jdbc url, the username, the password and a driver class.
     *
     * @param name            A connection name.
     * @param jdbcUrl         URL used for connection.
     * @param username        Username to use when connecting.
     * @param password        Password to use when connecting.
     * @param driverClassName Driver class name to use.
     */
    public MySQL(@NotNull String name, @NotNull String jdbcUrl,
                 @NotNull String username, @NotNull String password, @NotNull String driverClassName) {
        super(name, jdbcUrl, driverClassName);

        addDataSourceProperty("user", username);
        addDataSourceProperty("password", password);
    }

    /**
     * Gets a JDBC URL given the address to the database and the name of the database.
     * <p>
     * This method disables SSL connections.
     *
     * @param address      URL to the database to connect to.
     * @param databaseName The name of the database to connect to.
     * @return a JDBC URL
     */
    @NotNull
    public static String getUrl(@NotNull String address, @NotNull String databaseName) {
        return getUrl(address, databaseName, false);
    }

    /**
     * Gets a JDBC URL given the address to the database and the name of the database.
     *
     * @param address      URL to the database to connect to.
     * @param databaseName The name of the database to connect to.
     * @param isUsingSSL   True if SSL connection is used, false otherwise.
     * @return a JDBC URL
     */
    @NotNull
    public static String getUrl(@NotNull String address, @NotNull String databaseName, boolean isUsingSSL) {
        Objects.requireNonNull(address, "address");
        Objects.requireNonNull(databaseName, "databaseName");

        String url = "jdbc:mysql://" + address + "/" + databaseName;

        return isUsingSSL ?
                url + "?verifyServerCertificate=false&useSSL=true" :
                url + "?useSSL=false";
    }

    /**
     * Add settings for connection.
     * <p>
     * This method sets the following:
     *
     * <ul>
     *     <li>MaximumPoolSize : 10 ({@link HikariConfig#setMaximumPoolSize(int)}</li>
     *     <li>MinimumIdle : 10 ({@link HikariConfig#setMinimumIdle(int)}</li>
     *     <li>MaxLifetime : 600000 ({@link HikariConfig#setMaxLifetime(long)}</li>
     *     <li>ConnectionTimeout : 5000 ({@link HikariConfig#setConnectionTimeout(long)}</li>
     *     <li>InitializationFailTimeout : -1 ({@link HikariConfig#setInitializationFailTimeout(long)}</li>
     *     <li></li>
     *     <li>cachePrepStmts : true</li>
     *     <li>prepStmtCacheSize : 250</li>
     *     <li>prepStmtCacheSqlLimit : 2048</li>
     *     <li>useServerPrepStmts : true</li>
     *     <li>useLocalSessionState : true</li>
     *     <li>rewriteBatchedStatements : true</li>
     *     <li>cacheResultSetMetadata : true</li>
     *     <li>cacheServerConfiguration : true</li>
     *     <li>elideSetAutoCommits : true</li>
     *     <li>maintainTimeStats : false</li>
     * </ul>
     *
     * @param config A config to set
     */
    @Override
    protected void addProperties(@NotNull HikariConfig config) {
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(10);
        config.setMaxLifetime(600000);
        config.setConnectionTimeout(5000);
        config.setInitializationFailTimeout(-1);

        addDataSourceProperty("cachePrepStmts", true);
        addDataSourceProperty("prepStmtCacheSize", 250);
        addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        addDataSourceProperty("useServerPrepStmts", true);
        addDataSourceProperty("useLocalSessionState", true);
        addDataSourceProperty("rewriteBatchedStatements", true);
        addDataSourceProperty("cacheResultSetMetadata", true);
        addDataSourceProperty("cacheServerConfiguration", true);
        addDataSourceProperty("elideSetAutoCommits", true);
        addDataSourceProperty("maintainTimeStats", false);
    }

    /**
     * Returns the database type.
     *
     * @return {@link Database.Type#MYSQL}
     */
    @Override
    @NotNull
    public Type getType() {
        return Type.MYSQL;
    }
}
