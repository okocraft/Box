package net.okocraft.box.storage.implementation.database.database.sqlite;

import net.okocraft.box.storage.api.registry.StorageContext;
import net.okocraft.box.storage.implementation.database.table.CustomDataTable;
import net.okocraft.box.storage.implementation.database.table.LegacyCustomDataTable;
import net.okocraft.box.storage.implementation.database.table.MetaTable;
import net.okocraft.box.test.shared.storage.test.CustomDataStorageTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class SQLiteCustomDataConvertTest {

    @Test
    void test(@TempDir Path dir) throws Exception {
        var context = new StorageContext<>(dir, new SQLiteSetting("box_", "box-sqlite.db"));

        { // loading/saving custom data from/to legacy table
            var database = new SQLiteDatabase(context);
            try {
                database.prepare();

                var table = new LegacyCustomDataTable(database, true);
                CustomDataStorageTest.testVisit(table, true);
            } finally {
                database.shutdown();
            }
        }

        { // convert and checks data
            var database = new SQLiteDatabase(context);
            try {
                database.prepare();

                var meta = new MetaTable(database);
                meta.init();

                var table = new CustomDataTable(database, meta);
                table.updateFormatIfNeeded();
                CustomDataStorageTest.testVisit(table, false);
            } finally {
                database.shutdown();
            }
        }
    }
}
