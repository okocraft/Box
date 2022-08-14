package net.okocraft.box.storage.implementation.database;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.table.CustomDataTable;
import net.okocraft.box.storage.implementation.database.table.ItemTable;
import net.okocraft.box.storage.implementation.database.table.MetaTable;
import net.okocraft.box.storage.implementation.database.table.StockTable;
import net.okocraft.box.storage.implementation.database.table.UserTable;
import org.jetbrains.annotations.NotNull;

public class DatabaseStorage implements Storage {

    private final Database database;
    private final MetaTable metaTable;
    private final UserTable userTable;
    private final ItemTable itemTable;
    private final StockTable stockTable;
    private final CustomDataTable customDataTable;

    public DatabaseStorage(@NotNull Database database) {
        this.database = database;
        this.metaTable = new MetaTable(database);
        this.userTable = new UserTable(database);
        this.itemTable = new ItemTable(database, metaTable);
        this.stockTable = new StockTable(database);
        this.customDataTable = new CustomDataTable(database);
    }

    @Override
    public @NotNull String getName() {
        return database.getType().getName();
    }

    @Override
    public void init() throws Exception {
        database.prepare();

        metaTable.init();
        userTable.init();
        itemTable.init();
        stockTable.init();
        customDataTable.init();
    }

    @Override
    public void close() throws Exception {
        database.shutdown();
    }

    @Override
    public @NotNull ItemStorage getItemStorage() {
        return itemTable;
    }

    @Override
    public @NotNull UserStorage getUserStorage() {
        return userTable;
    }

    @Override
    public @NotNull StockStorage getStockStorage() {
        return stockTable;
    }

    @Override
    public @NotNull CustomDataStorage getCustomDataStorage() {
        return customDataTable;
    }
}
