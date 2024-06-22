package net.okocraft.box.storage.implementation.database.database.sqlite;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.registry.StorageContext;
import net.okocraft.box.storage.implementation.database.DatabaseStorage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;

public class SQLiteDatabase extends AbstractSQLiteDatabase {

    @Contract("_ -> new")
    public static @NotNull DatabaseStorage createStorage(@NotNull StorageContext<SQLiteSetting> context) {
        return new DatabaseStorage(new SQLiteDatabase(context));
    }

    private final Path databasePath;

    private SQLiteDatabase(@NotNull StorageContext<SQLiteSetting> context) {
        super(context.setting().tablePrefix());
        this.databasePath = context.pluginDirectory().resolve(context.setting().filename());
    }

    @Override
    public void prepare() throws Exception {
        if (!Files.exists(this.databasePath)) {
            Files.createDirectories(this.databasePath.getParent());
            Files.createFile(this.databasePath);
        }

        this.connect();

        try (var statement = this.getConnection().createStatement()) {
            statement.execute("PRAGMA journal_mode=TRUNCATE");
        }
    }

    @Override
    public void shutdown() throws Exception {
        try (var connection = this.getConnection();
             var statement = connection.prepareStatement("VACUUM")) {
            statement.execute();
        } finally {
            this.disconnect();
        }
    }

    @Override
    public @NotNull List<Storage.Property> getInfo() {
        return List.of(
                Storage.Property.of("type", "sqlite"),
                Storage.Property.of("table-prefix", this.tablePrefix),
                Storage.Property.of("database-filename", this.databasePath.getFileName().toString())
        );
    }

    @Override
    protected @NotNull Connection createConnection() throws ReflectiveOperationException {
        var filepath = this.databasePath.toAbsolutePath().toString();
        return newConnection(filepath, filepath);
    }
}
