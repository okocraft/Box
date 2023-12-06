package net.okocraft.box.storage.migrator.implementation;

import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;

public class StockMigrator extends AbstractDataMigrator<ItemMigrator.Result, StockStorage> {

    private final ItemMigrator.Result itemMigratorResult;

    public StockMigrator(@NotNull ItemMigrator.Result itemMigratorResult) {
        this.itemMigratorResult = itemMigratorResult;
    }

    @Override
    protected @NotNull StockStorage getDataStorage(@NotNull Storage storage) {
        return storage.getStockStorage();
    }

    @Override
    protected @NotNull ItemMigrator.Result migrateData(@NotNull StockStorage source, @NotNull StockStorage target, boolean debug) throws Exception {
        var users = this.itemMigratorResult.users();

        for (var user : users) {
            var stockData = source.loadStockData(user.getUUID());
            target.saveStockData(user.getUUID(), stockData, this.itemMigratorResult.itemIdMap());

            if (debug) {
                BoxLogger.logger().info("Migrated stock data: {}", stockData);
            }
        }

        BoxLogger.logger().info("All Stock data of {} users are migrated.", users.size());

        return this.itemMigratorResult;
    }
}
