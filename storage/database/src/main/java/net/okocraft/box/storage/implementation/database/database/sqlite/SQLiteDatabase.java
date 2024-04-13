package net.okocraft.box.storage.implementation.database.database.sqlite;

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
import java.util.Properties;

public class SQLiteDatabase implements Database {

    @Contract("_ -> new")
    public static @NotNull DatabaseStorage createStorage(@NotNull StorageContext<SQLiteSetting> context) {
        return new DatabaseStorage(new SQLiteDatabase(context));
    }

    private final String tablePrefix;
    private final SchemaSet schemaSet;
    private final Path databasePath;
    private final JournalMode journalMode;
    private NonCloseableConnection connection;

    @VisibleForTesting
    SQLiteDatabase(@NotNull StorageContext<SQLiteSetting> context) {
        this.tablePrefix = context.setting().tablePrefix();
        this.schemaSet = SQLiteTableSchema.create(tablePrefix);
        this.databasePath = context.pluginDirectory().resolve(context.setting().filename());
        this.journalMode = context.migrationMode() ? JournalMode.OFF : JournalMode.TRUNCATE ;
    }

    @Override
    public void prepare() throws Exception {
        if (!Files.exists(databasePath)) {
            createDatabaseFile();
        }

        this.connection = new NonCloseableConnection(this.createConnection());
        this.changePragma("journal_mode=" + this.journalMode.name());

        if (this.journalMode == JournalMode.OFF) {
            this.changePragma("sync_mode=off");
        }
    }

    @Override
    public void shutdown() throws Exception {
        try (var connection = this.getConnection();
             var statement = connection.prepareStatement("VACUUM")) {
            statement.execute();
        } finally {
            this.connection.shutdown();
        }
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
        if (this.connection == null) {
            throw new IllegalStateException("This database is not initialized.");
        }

        return this.connection;
    }

    private void createDatabaseFile() throws IOException {
        var parent = this.databasePath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.createFile(this.databasePath);
    }

    private @NotNull Connection createConnection() throws ReflectiveOperationException {
        var filepath = this.databasePath.toAbsolutePath();
        return (Connection) Class.forName("org.sqlite.jdbc4.JDBC4Connection")
                .getConstructor(String.class, String.class, Properties.class)
                .newInstance("jdbc:sqlite:" + filepath, filepath.toString(), new Properties());
    }

    private void changePragma(String query) throws SQLException {
        try (var statement = this.getConnection().prepareStatement("PRAGMA " + query)) {
            statement.execute();
        }
    }

    private enum JournalMode {
        OFF, TRUNCATE
    }
}
