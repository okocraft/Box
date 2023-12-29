package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.storage.api.registry.StorageContext;
import net.okocraft.box.storage.implementation.database.database.sqlite.SQLiteDatabase;
import net.okocraft.box.storage.implementation.database.database.sqlite.SQLiteSetting;
import net.okocraft.box.test.shared.storage.test.CommonUserStorageTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class SQLiteUserTableTest extends CommonUserStorageTest {

    @Test
    void testLoadingAndSaving(@TempDir Path dir) throws Exception {
        var db1 = setupDatabase(dir);
        try {
            this.testLoadingAndSaving(new UserTable(db1));
        } finally {
            db1.shutdown();
        }

        var db2 = setupDatabase(dir);
        try {
            this.testLoadingFromNewlyCreatedStorage(new UserTable(db2));
        } finally {
            db2.shutdown();
        }
    }

    @Test
    void testRename(@TempDir Path dir) throws Exception {
        var db1 = setupDatabase(dir);
        try {
            this.testRename(new UserTable(db1));
        } finally {
            db1.shutdown();
        }
    }

    private static @NotNull SQLiteDatabase setupDatabase(Path dir) throws Exception {
        var database = new SQLiteDatabase(new StorageContext<>(dir, new SQLiteSetting("box_", "box-sqlite.db")));
        database.prepare();
        return database;
    }
}
