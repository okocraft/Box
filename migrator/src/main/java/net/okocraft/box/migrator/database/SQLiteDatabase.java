package net.okocraft.box.migrator.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

/*
 * source:
 * https://github.com/okocraft/Box/blob/master/src/main/java/net/okocraft/box/database/Database.java
 */
public class SQLiteDatabase implements Database {

    private final HikariDataSource hikari;

    public SQLiteDatabase(@NotNull Path path) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + path);
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
