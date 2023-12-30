package net.okocraft.box.storage.implementation.database.database.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.registry.StorageContext;
import net.okocraft.box.storage.implementation.database.DatabaseStorage;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.schema.SchemaSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SQLiteDatabase implements Database {

    @Contract("_ -> new")
    public static @NotNull DatabaseStorage createStorage(@NotNull StorageContext<SQLiteSetting> context) {
        return new DatabaseStorage(new SQLiteDatabase(context));
    }

    private final String tablePrefix;
    private final SchemaSet schemaSet;
    private final Path databasePath;
    private HikariDataSource hikariDataSource;

    @VisibleForTesting
    SQLiteDatabase(@NotNull StorageContext<SQLiteSetting> context) {
        this.tablePrefix = context.setting().tablePrefix();
        this.schemaSet = SQLiteTableSchema.create(tablePrefix);
        this.databasePath = context.pluginDirectory().resolve(context.setting().filename());
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
        config.setMaximumPoolSize(50);

        hikariDataSource = new HikariDataSource(config);
    }

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
