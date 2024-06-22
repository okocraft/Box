package net.okocraft.box.storage.implementation.database.database.sqlite;

import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.List;

final class MemorySQLiteDatabase extends AbstractSQLiteDatabase {

    static @NotNull MemorySQLiteDatabase prepareDatabase() throws Exception {
        var database = new MemorySQLiteDatabase();
        database.prepare();
        return database;
    }

    MemorySQLiteDatabase() {
        super("box_");
    }

    @Override
    public void prepare() throws Exception {
        this.connect();
    }

    @Override
    public void shutdown() throws Exception {
        this.disconnect();
    }

    @Override
    protected @NotNull Connection createConnection() throws Exception {
        return newConnection("memory", ":memory:");
    }

    @Override
    public @NotNull List<Storage.Property> getInfo() {
        return List.of(
                Storage.Property.of("type", "sqlite-memory"),
                Storage.Property.of("table-prefix", this.tablePrefix)
        );
    }
}
