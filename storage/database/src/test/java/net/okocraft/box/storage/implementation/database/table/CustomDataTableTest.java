package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.test.shared.storage.test.CustomDataStorageTest;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public abstract class CustomDataTableTest extends CustomDataStorageTest<Database> {

    protected static @NotNull AbstractCustomDataTable newCustomDataTable(@NotNull Database database) throws Exception {
        MetaTable metaTable = new MetaTable(database);
        CustomDataTable table = new CustomDataTable(database, metaTable);

        try (Connection connection = database.getConnection()) {
            metaTable.init(connection);
            table.init(connection);
        }

        return table;
    }

    protected static @NotNull LegacyCustomDataTable newLegacyCustomDataTable(@NotNull Database database) throws Exception {
        LegacyCustomDataTable table = new LegacyCustomDataTable(database, true);

        try (Connection connection = database.getConnection()) {
            table.init(connection);
        }

        return table;
    }
}
