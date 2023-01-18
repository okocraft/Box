package net.okocraft.box.storage.implementation.database.database.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.schema.SchemaSet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SQLiteDatabase implements Database {

    private final String tablePrefix;
    private final SchemaSet schemaSet;
    private final Path databasePath;
    private HikariDataSource hikariDataSource;

    public SQLiteDatabase(@NotNull Path databasePath, @NotNull String tablePrefix) {
        this.tablePrefix = tablePrefix;
        this.schemaSet = SQLiteTableSchema.create(tablePrefix);
        this.databasePath = databasePath;
    }

    @Override
    public void prepare() throws Exception {
        if (!Files.exists(databasePath)) {
            createDatabaseFile();
        }

        var config = new HikariConfig();
        config.setPoolName("BoxSQLitePool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + databasePath.toAbsolutePath());
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000);
        config.setIdleTimeout(45000);
        config.setMaximumPoolSize(50);

        hikariDataSource = new HikariDataSource(config);
    }

    @SuppressWarnings("SqlDialectInspection")
    @Override
    public void shutdown() throws Exception {
        try (var connection = getConnection();
             var statement = connection.prepareStatement("VACUUM")) {
            statement.execute();
        }

        hikariDataSource.close();
    }

    @Override
    public @NotNull Type getType() {
        return Type.SQLITE;
    }

    @Override
    public @NotNull List<Storage.Property> getInfo() {
        return List.of(
                Storage.Property.of("table-prefix", tablePrefix),
                Storage.Property.of("database-filename", databasePath.getFileName().toString())
        );
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

    private void createDatabaseFile() throws IOException {
        var parent = databasePath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.createFile(databasePath);
    }
}
