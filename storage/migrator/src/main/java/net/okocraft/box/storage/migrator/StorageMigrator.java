package net.okocraft.box.storage.migrator;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.util.item.DefaultItemProvider;
import net.okocraft.box.storage.migrator.implementation.CustomDataMigrator;
import net.okocraft.box.storage.migrator.implementation.ItemMigrator;
import net.okocraft.box.storage.migrator.implementation.StockMigrator;
import net.okocraft.box.storage.migrator.implementation.UserMigrator;
import net.okocraft.box.storage.migrator.util.LoggerWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class StorageMigrator {

    private final Storage sourceStorage;
    private final Storage targetStorage;
    private final DefaultItemProvider defaultItemProvider;
    private final LoggerWrapper logger;

    public StorageMigrator(@NotNull Storage source, @NotNull Storage target, @NotNull DefaultItemProvider defaultItemProvider, @NotNull Logger logger, boolean debug) {
        this.sourceStorage = source;
        this.targetStorage = target;
        this.defaultItemProvider = defaultItemProvider;
        this.logger = new LoggerWrapper(logger, debug);
    }

    public void init() throws Exception {
        sourceStorage.init();
        targetStorage.init();
    }

    public void run() throws Exception {
        new UserMigrator()
                .next(result -> new ItemMigrator(result, this.defaultItemProvider))
                .next(StockMigrator::new)
                .next(CustomDataMigrator::new)
                .migrate(this.sourceStorage, this.targetStorage, this.logger);
    }

    public void close() throws Exception {
        sourceStorage.close();
        targetStorage.close();
    }
}
