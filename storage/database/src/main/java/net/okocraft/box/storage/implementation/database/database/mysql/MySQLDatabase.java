package net.okocraft.box.storage.implementation.database.database.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.schema.SchemaSet;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class MySQLDatabase implements Database {

    private final SchemaSet schemaSet;
    private final MySQLConfig mySQLConfig;
    private HikariDataSource hikariDataSource;

    public MySQLDatabase(@NotNull MySQLConfig config) {
        this.mySQLConfig = config;
        this.schemaSet = MySQLTableSchema.create(config.tablePrefix);
    }

    @Override
    public void prepare() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver"); // checks if the driver exists

        var config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" + mySQLConfig.address + ":" + mySQLConfig.port + "/" + mySQLConfig.databaseName);
        config.setUsername(mySQLConfig.username);
        config.setPassword(mySQLConfig.password);

        config.setPoolName("BoxMySQLPool");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000);
        config.setMaximumPoolSize(50);

        configureDataSourceProperties(config.getDataSourceProperties());

        hikariDataSource = new HikariDataSource(config);
    }

    @Override
    public void shutdown() {
        hikariDataSource.close();
    }

    @Override
    public @NotNull Type getType() {
        return Type.MYSQL;
    }

    @Override
    public @NotNull List<Storage.Property> getInfo() {
        var result = new ArrayList<Storage.Property>();

        result.add(Storage.Property.of("database-name", mySQLConfig.databaseName));
        result.add(Storage.Property.of("table-prefix", mySQLConfig.tablePrefix));

        var ping = ping();
        if (0 < ping) {
            result.add(Storage.Property.of("ping", ping + "ms"));
        }

        return result;
    }

    @Override
    public @NotNull SchemaSet getSchemaSet() {
        return schemaSet;
    }

    @Override
    public @NotNull Connection getConnection() throws SQLException {
        if (hikariDataSource == null) {
            throw new IllegalStateException("HikariDataSource is not initialized.");
        }

        return hikariDataSource.getConnection();
    }

    private void configureDataSourceProperties(@NotNull Properties properties) {
        // https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery
        properties.putIfAbsent("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));

        // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        properties.putIfAbsent("cachePrepStmts", "true");
        properties.putIfAbsent("prepStmtCacheSize", "250");
        properties.putIfAbsent("prepStmtCacheSqlLimit", "2048");
        properties.putIfAbsent("useServerPrepStmts", "true");
        properties.putIfAbsent("useLocalSessionState", "true");
        properties.putIfAbsent("rewriteBatchedStatements", "true");
        properties.putIfAbsent("cacheResultSetMetadata", "true");
        properties.putIfAbsent("cacheServerConfiguration", "true");
        properties.putIfAbsent("elideSetAutoCommits", "true");
        properties.putIfAbsent("maintainTimeStats", "false");
        properties.putIfAbsent("alwaysSendSetIsolation", "false");
        properties.putIfAbsent("cacheCallableStmts", "true");
    }

    private long ping() {
        if (hikariDataSource == null) {
            return -1;
        }

        long start = System.currentTimeMillis();

        try (var connection = getConnection(); var statement = connection.createStatement()) {
            statement.execute("SELECT 1");
        } catch (Exception ignored) {
            return -1;
        }

        return System.currentTimeMillis() - start;
    }
}
