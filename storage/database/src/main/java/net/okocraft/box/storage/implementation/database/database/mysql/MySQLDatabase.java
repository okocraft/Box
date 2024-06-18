package net.okocraft.box.storage.implementation.database.database.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.registry.StorageContext;
import net.okocraft.box.storage.implementation.database.DatabaseStorage;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.operator.OperatorProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class MySQLDatabase implements Database {

    @Contract("_ -> new")
    public static @NotNull DatabaseStorage createStorage(@NotNull StorageContext<MySQLSetting> context) {
        return new DatabaseStorage(new MySQLDatabase(context));
    }

    private final OperatorProvider operators;
    private final MySQLSetting mySQLSetting;
    private HikariDataSource hikariDataSource;

    public MySQLDatabase(@NotNull StorageContext<MySQLSetting> context) {
        this.mySQLSetting = context.setting();
        this.operators = MySQLOperators.create(this.mySQLSetting.tablePrefix());
    }

    @Override
    public void prepare() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver"); // checks if the driver exists

        var config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" + this.mySQLSetting.address() + ":" + this.mySQLSetting.port() + "/" + this.mySQLSetting.databaseName());
        config.setUsername(this.mySQLSetting.username());
        config.setPassword(this.mySQLSetting.password());

        config.setPoolName("BoxMySQLPool");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000);
        config.setMaximumPoolSize(50);

        this.configureDataSourceProperties(config.getDataSourceProperties());

        this.hikariDataSource = new HikariDataSource(config);
    }

    @Override
    public void shutdown() {
        this.hikariDataSource.close();
    }

    @Override
    public @NotNull List<Storage.Property> getInfo() {
        var result = new ArrayList<Storage.Property>();

        result.add(Storage.Property.of("type", "mysql"));
        result.add(Storage.Property.of("database-name", this.mySQLSetting.databaseName()));
        result.add(Storage.Property.of("table-prefix", this.mySQLSetting.tablePrefix()));

        var ping = this.ping();
        if (0 <= ping) {
            result.add(Storage.Property.of("ping", ping + "ms"));
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public @NotNull Connection getConnection() throws SQLException {
        if (this.hikariDataSource == null) {
            throw new IllegalStateException("HikariDataSource is not initialized.");
        }

        return this.hikariDataSource.getConnection();
    }

    @Override
    public @NotNull OperatorProvider operators() {
        return this.operators;
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
        if (this.hikariDataSource == null) {
            return -1;
        }

        long start = System.currentTimeMillis();

        try (var connection = this.getConnection(); var statement = connection.createStatement()) {
            statement.execute("SELECT 1");
        } catch (Exception ignored) {
            return -1;
        }

        return System.currentTimeMillis() - start;
    }
}
