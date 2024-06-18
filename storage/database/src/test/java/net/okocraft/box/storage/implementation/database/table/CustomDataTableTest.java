package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.test.shared.storage.test.CustomDataStorageTest;
import org.jetbrains.annotations.NotNull;

public abstract class CustomDataTableTest extends CustomDataStorageTest<Database> {

    protected static @NotNull AbstractCustomDataTable newCustomDataTable(@NotNull Database database) throws Exception {
        var metaTable = new MetaTable(database);
        var table = new CustomDataTable(database, metaTable);

        try (var connection = database.getConnection()) {
            metaTable.init(connection);
            table.init(connection);
        }

        return table;
    }

    protected static @NotNull LegacyCustomDataTable newLegacyCustomDataTable(@NotNull Database database) throws Exception {
        var table = new LegacyCustomDataTable(database, true);

        try (var connection = database.getConnection()) {
            table.init(connection);
        }

        return table;
    }
}
