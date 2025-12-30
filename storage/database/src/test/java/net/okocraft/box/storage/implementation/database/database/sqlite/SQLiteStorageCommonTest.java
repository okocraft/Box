package net.okocraft.box.storage.implementation.database.database.sqlite;

import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.CustomItemStorage;
import net.okocraft.box.storage.api.model.item.DefaultItemStorage;
import net.okocraft.box.storage.api.model.item.RemappedItemStorage;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.table.CustomDataTableTest;
import net.okocraft.box.storage.implementation.database.table.ItemTable;
import net.okocraft.box.storage.implementation.database.table.MetaTableTest;
import net.okocraft.box.storage.implementation.database.table.RemappedItemTable;
import net.okocraft.box.storage.implementation.database.table.StockTableTest;
import net.okocraft.box.storage.implementation.database.table.UserTableTest;
import net.okocraft.box.test.shared.storage.test.CustomItemStorageTest;
import net.okocraft.box.test.shared.storage.test.DefaultItemStorageTest;
import net.okocraft.box.test.shared.storage.test.RemappedItemStorageTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;

import java.sql.Connection;
import java.util.Optional;

class SQLiteStorageCommonTest {

    @Nested
    class Meta extends MetaTableTest {
        @Override
        protected @NotNull Database newDatabase() throws Exception {
            return MemorySQLiteDatabase.prepareDatabase();
        }
    }

    @Nested
    class User extends UserTableTest {
        @Override
        protected @NotNull Database newStorage() throws Exception {
            return MemorySQLiteDatabase.prepareDatabase();
        }
    }

    @Nested
    class DefaultItem extends DefaultItemStorageTest<MemorySQLiteDatabase> {
        @Override
        protected @NotNull MemorySQLiteDatabase newStorage() throws Exception {
            return MemorySQLiteDatabase.prepareDatabase();
        }

        @Override
        protected @NotNull DefaultItemStorage newDefaultItemStorage(@NotNull MemorySQLiteDatabase database) throws Exception {
            ItemTable table = new ItemTable(database);

            try (Connection connection = database.getConnection()) {
                table.init(connection);
            }

            return table;
        }
    }

    @Nested
    class CustomItem extends CustomItemStorageTest<MemorySQLiteDatabase> {
        @Override
        protected @NotNull MemorySQLiteDatabase newStorage() throws Exception {
            return MemorySQLiteDatabase.prepareDatabase();
        }

        @Override
        protected @NotNull CustomItemStorage newCustomItemStorage(@NotNull MemorySQLiteDatabase database) throws Exception {
            ItemTable table = new ItemTable(database);

            try (Connection connection = database.getConnection()) {
                table.init(connection);
            }

            return table.customItemStorage();
        }
    }

    @Nested
    class RemappedItem extends RemappedItemStorageTest<MemorySQLiteDatabase> {
        @Override
        protected @NotNull MemorySQLiteDatabase newStorage() throws Exception {
            return MemorySQLiteDatabase.prepareDatabase();
        }

        @Override
        protected @NotNull RemappedItemStorage newRemappedItemStorage(@NotNull MemorySQLiteDatabase database) throws Exception {
            RemappedItemTable table = new RemappedItemTable(database);

            try (Connection connection = database.getConnection()) {
                table.init(connection);
            }

            return table;
        }
    }

    @Nested
    class Stock extends StockTableTest {
        @Override
        protected @NotNull Database newStorage() throws Exception {
            return MemorySQLiteDatabase.prepareDatabase();
        }
    }

    @Nested
    class CustomData extends CustomDataTableTest {
        @Override
        protected @NotNull Database newStorage() throws Exception {
            return MemorySQLiteDatabase.prepareDatabase();
        }

        @Override
        protected @NotNull CustomDataStorage newCustomDataStorage(@NotNull Database database) throws Exception {
            return newCustomDataTable(database);
        }

        @Override
        protected @NotNull Optional<CustomDataStorage> newLegacyCustomDataStorage(@NotNull Database database) throws Exception {
            return Optional.of(newLegacyCustomDataTable(database));
        }
    }

    @Nested
    class LegacyCustomData extends CustomDataTableTest {
        @Override
        protected @NotNull Database newStorage() throws Exception {
            return MemorySQLiteDatabase.prepareDatabase();
        }

        @Override
        protected @NotNull CustomDataStorage newCustomDataStorage(@NotNull Database database) throws Exception {
            return newLegacyCustomDataTable(database);
        }
    }
}
