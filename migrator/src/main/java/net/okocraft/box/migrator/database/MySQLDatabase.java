package net.okocraft.box.migrator.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

/*
 * source:
 * https://github.com/okocraft/Box/blob/v3/master/src/main/java/net/okocraft/box/database/Database.java
 */
public class MySQLDatabase implements Database {

    private final HikariDataSource hikari;

    public MySQLDatabase(String host, int port, String user, String password, String dbName) {
        HikariConfig config = new HikariConfig();

        // login data
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false");
        config.setUsername(user);
        config.setPassword(password);

        // general mysql settings
        config.setMaxLifetime(600000L);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtsCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("rewriteBatchedStatements", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("maintainTimeStats", false);
        hikari = new HikariDataSource(config);
    }


    @Override
    public @NotNull Connection getConnection() throws SQLException {
        return hikari.getConnection();
    }

    @Override
    public void close() {
        hikari.close();
    }
}
