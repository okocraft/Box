package net.okocraft.box.storage.migrator.implementation;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.migrator.util.LoggerWrapper;
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
    protected @NotNull ItemMigrator.Result migrateData(@NotNull StockStorage source, @NotNull StockStorage target, @NotNull LoggerWrapper logger) throws Exception {
        var users = this.itemMigratorResult.users();

        for (var user : users) {
            var stockData = source.loadStockData(user.getUUID());
            target.saveStockData(user.getUUID(), stockData, this.itemMigratorResult.itemIdMap());

            if (logger.debug()) {
                logger.info("Migrated stock data: " + stockData);
            }
        }

        logger.info("All Stock data of " + users.size() + " users are migrated.");

        return this.itemMigratorResult;
    }
}
