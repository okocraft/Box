package net.okocraft.box.storage.implementation.database.patches;

import net.okocraft.box.storage.api.util.SneakyThrow;
import net.okocraft.box.storage.implementation.database.DatabaseStorage;
import net.okocraft.box.storage.implementation.database.table.ItemTable;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public final class V6Patch {

    public static void patch(@NotNull DatabaseStorage storage) throws SQLException {
        var operators = storage.getDatabase().operators();

        try (var connection = storage.getDatabase().getConnection()) {
            if (!operators.patcher().hasTable(connection, "items")) {
                return;
            }

            operators.patcher().renameTable(connection, "items", "legacy_items");

            operators.itemTable().initTable(connection);

            try (var statement = operators.itemTable().insertStatement(connection)) {
                operators.patcher().getDefaultItemsFromLegacy(
                    connection,
                    "legacy_items",
                    (id, name) -> {
                        try {
                            operators.itemTable().addInsertBatch(statement, id, name, ItemTable.DEFAULT_ITEM_TYPE);
                        } catch (SQLException e) {
                            SneakyThrow.sneaky(e);
                        }
                    }
                );
                statement.executeBatch();
            }

            operators.customItemTable().initTable(connection);

            try (var statement = operators.itemTable().insertStatement(connection)) {
                operators.patcher().getCustomItemsFromLegacyItemTable(
                    connection,
                    "legacy_items",
                    data -> {
                        try {
                            operators.itemTable().addInsertBatch(statement, data.internalId(), data.plainName(), ItemTable.CUSTOM_ITEM_TYPE);
                            operators.customItemTable().insert(connection, data.internalId(), data.itemData());
                        } catch (SQLException e) {
                            SneakyThrow.sneaky(e);
                        }
                    }
                );
                statement.executeBatch();
            }
        }
    }

    private V6Patch() {
        throw new UnsupportedOperationException();
    }
}
