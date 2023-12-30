package net.okocraft.box.storage.implementation.database.database.sqlite;

import net.okocraft.box.storage.api.registry.StorageContext;
import net.okocraft.box.storage.implementation.database.table.StockTable;
import net.okocraft.box.storage.implementation.database.table.UserTable;
import net.okocraft.box.test.shared.storage.test.StockStorageTest;
import net.okocraft.box.test.shared.storage.test.UserStorageTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class SQLiteStorageCommonTest {

    @Nested
    class User {

        @Test
        void testLoadingAndSaving(@TempDir Path dir) throws Exception {
            var db1 = setupDatabase(dir);
            try {
                UserStorageTest.testLoadingAndSaving(new UserTable(db1));
            } finally {
                db1.shutdown();
            }

            var db2 = setupDatabase(dir);
            try {
                UserStorageTest.testLoadingFromNewlyCreatedStorage(new UserTable(db2));
            } finally {
                db2.shutdown();
            }
        }

        @Test
        void testRename(@TempDir Path dir) throws Exception {
            var db1 = setupDatabase(dir);
            try {
                UserStorageTest.testRename(new UserTable(db1));
            } finally {
                db1.shutdown();
            }
        }

    }

    @Nested
    class Stock {
        @Test
        void testLoadingAndSaving(@TempDir Path dir) throws Exception {
            var db1 = setupDatabase(dir);
            try {
                StockStorageTest.testLoadingAndSaving(new StockTable(db1));
            } finally {
                db1.shutdown();
            }

            var db2 = setupDatabase(dir);
            try {
                StockStorageTest.testLoadingFromNewlyCreatedStorage(new StockTable(db2));
            } finally {
                db2.shutdown();
            }
        }

        @Test
        void testPartialSaving(@TempDir Path dir) throws Exception {
            var db = setupDatabase(dir);
            try {
                StockStorageTest.testPartialSaving(new StockTable(db));
            } finally {
                db.shutdown();
            }
        }

        @Test
        void testCleaningZeroStock(@TempDir Path dir) throws Exception {
            var db = setupDatabase(dir);
            try {
                StockStorageTest.testCleaningZeroStock(new StockTable(db));
            } finally {
                db.shutdown();
            }
        }
    }

    private static @NotNull SQLiteDatabase setupDatabase(Path dir) throws Exception {
        var database = new SQLiteDatabase(new StorageContext<>(dir, new SQLiteSetting("box_", "box-sqlite.db")));
        database.prepare();
        return database;
    }
}
