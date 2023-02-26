package net.okocraft.box.storage.migrator.data;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.migrator.StorageMigrator;
import net.okocraft.box.storage.migrator.util.LoggerWrapper;
import net.okocraft.box.storage.migrator.util.MigratedBoxUsers;
import org.jetbrains.annotations.NotNull;

public class StockMigrator implements DataMigrator<StockStorage> {

    @Override
    public @NotNull StockStorage getDataStorage(@NotNull Storage storage) {
        return storage.getStockStorage();
    }

    @Override
    public void migrate(@NotNull StockStorage source, @NotNull StockStorage target, @NotNull LoggerWrapper logger) throws Exception {
        for (var user : MigratedBoxUsers.LIST) {
            var userStockHolder = source.loadUserStockHolder(user);
            target.saveUserStockHolder(userStockHolder);

            if (StorageMigrator.debug) {
                logger.info("Migrated stock data: " + userStockHolder);
            }
        }

        logger.info("All Stock data of " + MigratedBoxUsers.LIST.size() + " users are migrated.");
    }
}
