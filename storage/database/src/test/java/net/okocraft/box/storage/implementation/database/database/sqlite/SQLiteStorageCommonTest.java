package net.okocraft.box.storage.implementation.database.database.sqlite;

import net.okocraft.box.storage.api.registry.StorageContext;
import net.okocraft.box.storage.implementation.database.table.CustomDataTable;
import net.okocraft.box.storage.implementation.database.table.LegacyCustomDataTable;
import net.okocraft.box.storage.implementation.database.table.MetaTable;
import net.okocraft.box.storage.implementation.database.table.StockTable;
import net.okocraft.box.storage.implementation.database.table.UserTable;
import net.okocraft.box.test.shared.storage.test.CustomDataStorageTest;
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

    @Nested
    class CustomData {

        @Test
        void testSaving(@TempDir Path dir) throws Exception {
            var db1 = setupDatabase(dir);
            try {
                var metaTable = new MetaTable(db1);
                metaTable.init();
                CustomDataStorageTest.testLoadingAndSaving(new CustomDataTable(db1, metaTable));
            } finally {
                db1.shutdown();
            }

            var db2 = setupDatabase(dir);
            try {
                var metaTable = new MetaTable(db2);
                metaTable.init();
                CustomDataStorageTest.testLoadingFromNewlyCreatedStorage(new CustomDataTable(db2, metaTable));
            } finally {
                db2.shutdown();
            }
        }

        @Test
        void testVisit(@TempDir Path dir) throws Exception {
            var db = setupDatabase(dir);
            try {
                var metaTable = new MetaTable(db);
                metaTable.init();
                CustomDataStorageTest.testVisit(new CustomDataTable(db, metaTable), true);
            } finally {
                db.shutdown();
            }
        }
    }

    @Nested
    class CustomData_Legacy {

        @Test
        void testSaving(@TempDir Path dir) throws Exception {
            var db1 = setupDatabase(dir);
            try {
                CustomDataStorageTest.testLoadingAndSaving(new LegacyCustomDataTable(db1, true));
            } finally {
                db1.shutdown();
            }

            var db2 = setupDatabase(dir);
            try {
                CustomDataStorageTest.testLoadingFromNewlyCreatedStorage(new LegacyCustomDataTable(db2, true));
            } finally {
                db2.shutdown();
            }
        }

        @Test
        void testVisit(@TempDir Path dir) throws Exception {
            var db = setupDatabase(dir);
            try {
                CustomDataStorageTest.testVisit(new LegacyCustomDataTable(db, true), true);
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
