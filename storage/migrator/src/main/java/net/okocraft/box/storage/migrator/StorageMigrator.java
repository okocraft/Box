package net.okocraft.box.storage.migrator;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.util.item.BoxItemSupplier;
import net.okocraft.box.storage.migrator.data.CustomDataMigrator;
import net.okocraft.box.storage.migrator.data.DataMigrator;
import net.okocraft.box.storage.migrator.data.ItemMigrator;
import net.okocraft.box.storage.migrator.data.StockMigrator;
import net.okocraft.box.storage.migrator.data.UserMigrator;
import net.okocraft.box.storage.migrator.util.LoggerWrapper;
import net.okocraft.box.storage.migrator.util.MigratedBoxUsers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class StorageMigrator {

    public static boolean debug = false;

    private final Storage sourceStorage;
    private final Storage targetStorage;
    private final LoggerWrapper logger;

    private final List<DataMigrator<?>> migratorList = List.of(
            new UserMigrator(),
            new ItemMigrator(),
            new StockMigrator(),
            new CustomDataMigrator()
    );

    public StorageMigrator(@NotNull Storage source, @NotNull Storage target, @Nullable Logger logger) {
        this.sourceStorage = source;
        this.targetStorage = target;
        this.logger = new LoggerWrapper(logger);
    }

    public void init() throws Exception {
        sourceStorage.init();
        targetStorage.init();
    }

    public void run() throws Exception {
        for (var migrator : migratorList) {
            logger.info("Starting " + migrator.getClass().getSimpleName() + "...");
            this.migrate(migrator);
        }
    }

    public void close() throws Exception {
        MigratedBoxUsers.LIST = Collections.emptyList();
        BoxItemSupplier.ITEM_FUNCTION = null;

        sourceStorage.close();
        targetStorage.close();
    }

    private <T> void migrate(@NotNull DataMigrator<T> migrator) throws Exception {
        var source = migrator.getDataStorage(sourceStorage);
        var target = migrator.getDataStorage(targetStorage);
        migrator.migrate(source, target, logger);
    }
}
