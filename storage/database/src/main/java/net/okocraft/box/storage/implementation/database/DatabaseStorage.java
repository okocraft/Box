package net.okocraft.box.storage.implementation.database;

import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.CustomItemStorage;
import net.okocraft.box.storage.api.model.item.DefaultItemStorage;
import net.okocraft.box.storage.api.model.item.RemappedItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.model.version.StorageVersion;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.patches.V6Patch;
import net.okocraft.box.storage.implementation.database.table.CustomDataTable;
import net.okocraft.box.storage.implementation.database.table.ItemTable;
import net.okocraft.box.storage.implementation.database.table.MetaTable;
import net.okocraft.box.storage.implementation.database.table.RemappedItemTable;
import net.okocraft.box.storage.implementation.database.table.StockTable;
import net.okocraft.box.storage.implementation.database.table.UserTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.List;

public class DatabaseStorage implements Storage {

    private final Database database;
    private final MetaTable metaTable;
    private final UserTable userTable;
    private final ItemTable itemTable;
    private final StockTable stockTable;
    private final CustomDataTable customDataTable;
    private final RemappedItemTable remappedItemTable;

    private boolean firstStartup;

    public DatabaseStorage(@NotNull Database database) {
        this.database = database;
        this.metaTable = new MetaTable(database);
        this.userTable = new UserTable(database);
        this.itemTable = new ItemTable(database);
        this.remappedItemTable = new RemappedItemTable(database);
        this.stockTable = new StockTable(database);
        this.customDataTable = new CustomDataTable(database, this.metaTable);
    }

    @Override
    public @NotNull List<Property> getInfo() {
        return this.database.getInfo();
    }

    @Override
    public void init() throws Exception {
        this.database.prepare();

        try (Connection connection = this.database.getConnection()) {
            this.firstStartup = !this.metaTable.exists(connection);
            if (this.firstStartup) {
                this.metaTable.init(connection);
            }
        }
    }

    @Override
    public boolean isFirstStartup() {
        return this.firstStartup;
    }

    @Override
    public void prepare() throws Exception {
        try (Connection connection = this.database.getConnection()) {
            this.userTable.init(connection);
            this.itemTable.init(connection);
            this.remappedItemTable.init(connection);
            this.stockTable.init(connection);
            this.customDataTable.init(connection);
        }
    }

    @Override
    public void close() throws Exception {
        this.database.shutdown();
    }

    @Override
    public @NotNull DefaultItemStorage defaultItemStorage() {
        return this.itemTable;
    }

    @Override
    public @NotNull CustomItemStorage customItemStorage() {
        return this.itemTable.customItemStorage();
    }

    @Override
    public @NotNull RemappedItemStorage remappedItemStorage() {
        return this.remappedItemTable;
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

    @Override
    public @Nullable MCDataVersion getDataVersion() throws Exception {
        return this.metaTable.getItemDataVersion();
    }

    @Override
    public void saveDataVersion(@NotNull MCDataVersion version) throws Exception {
        this.metaTable.saveItemDataVersion(version);
    }

    @Override
    public @NotNull StorageVersion getStorageVersion() throws Exception {
        return this.metaTable.getStorageVersion();
    }

    @Override
    public void saveStorageVersion(@NotNull StorageVersion version) throws Exception {
        this.metaTable.saveStorageVersion(version);
    }

    @Override
    public void applyStoragePatches(@NotNull StorageVersion current, @NotNull StorageVersion latest) throws Exception {
        if (current.isBefore(StorageVersion.V6) && latest.isAfterOrSame(StorageVersion.V6)) {
            BoxLogger.logger().info("Applying v6 patches for database...");
            V6Patch.patch(this);
        }
    }

    public @NotNull Database getDatabase() {
        return this.database;
    }
}
