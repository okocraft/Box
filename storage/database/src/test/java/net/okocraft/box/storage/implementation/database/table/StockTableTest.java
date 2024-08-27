package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.test.shared.storage.test.StockStorageTest;
import org.jetbrains.annotations.NotNull;

public abstract class StockTableTest extends StockStorageTest<Database> {
    @Override
    protected @NotNull StockTable newStockStorage(@NotNull Database database) throws Exception {
        var table = new StockTable(database);

        try (var connection = database.getConnection()) {
            table.init(connection);
        }

        return table;
    }
}
