package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.version.StorageVersion;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class MetaTableTest {

    @Test
    void testItemDataVersion() throws Exception {
        try (var database = this.newDatabase()) {
            var table = newTable(database);

            assertNull(table.getItemDataVersion());

            var saved = MCDataVersion.MC_1_20_5;
            table.saveItemDataVersion(saved);
            assertEquals(saved, table.getItemDataVersion());

            var updated = MCDataVersion.MC_1_20_6;
            table.saveItemDataVersion(updated);
            assertEquals(updated, table.getItemDataVersion());
        }
    }

    @Test
    void testStorageVersion() throws Exception {
        try (var database = this.newDatabase()) {
            var table = newTable(database);

            assertEquals(StorageVersion.BEFORE_V6, table.getStorageVersion());

            var saved = StorageVersion.V6;
            table.saveStorageVersion(saved);
            assertEquals(saved, table.getStorageVersion());

            var updated = new StorageVersion(100);
            table.saveStorageVersion(updated);
            assertEquals(updated, table.getStorageVersion());
        }
    }

    @Test
    void testCustomDataFormat() throws Exception {
        try (var database = this.newDatabase()) {
            var table = newTable(database);
            assertFalse(table.isCurrentCustomDataFormat());
            table.saveCurrentCustomDataFormat();
            assertTrue(table.isCurrentCustomDataFormat());
        }
    }

    protected abstract @NotNull Database newDatabase() throws Exception;

    private static @NotNull MetaTable newTable(@NotNull Database database) throws Exception {
        var table = new MetaTable(database);

        try (var connection = database.getConnection()) {
            table.init(connection);
        }

        return table;
    }
}
