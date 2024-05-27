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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        this.itemTable = new ItemTable(database, this.metaTable);
        this.stockTable = new StockTable(database);
        this.customDataTable = new CustomDataTable(database, this.metaTable);
    }

    @Override
    public @NotNull String getName() {
        return this.database.getType().getName();
    }

    @Override
    public @NotNull List<Property> getInfo() {
        var result = new ArrayList<Property>();
        result.add(Property.of("database-type", this.database.getType().getName()));
        result.addAll(this.database.getInfo());
        return Collections.unmodifiableList(result);
    }

    @Override
    public void init() throws Exception {
        this.database.prepare();

        this.metaTable.init();
        this.userTable.init();
        this.itemTable.init();
        this.stockTable.init();
        this.customDataTable.init();
    }

    @Override
    public void close() throws Exception {
        this.database.shutdown();
    }

    @Override
    public @NotNull ItemStorage getItemStorage() {
        return this.itemTable;
    }

    @Override
    public @NotNull UserStorage getUserStorage() {
        return this.userTable;
    }

    @Override
    public @NotNull StockStorage getStockStorage() {
        return this.stockTable;
    }

    @Override
    public @NotNull CustomDataStorage getCustomDataStorage() {
        return this.customDataTable;
    }

    public @NotNull Database getDatabase() {
        return this.database;
    }
}
