package net.okocraft.box.storage.migrator.implementation;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.migrator.util.LoggerWrapper;
import org.jetbrains.annotations.NotNull;

public class CustomDataMigrator extends AbstractDataMigrator<ItemMigrator.Result, CustomDataStorage> {

    private final ItemMigrator.Result itemMigratorResult;

    public CustomDataMigrator(@NotNull ItemMigrator.Result itemMigratorResult) {
        this.itemMigratorResult = itemMigratorResult;
    }

    @Override
    protected @NotNull CustomDataStorage getDataStorage(@NotNull Storage storage) {
        return storage.getCustomDataStorage();
    }

    @Override
    protected @NotNull ItemMigrator.Result migrateData(@NotNull CustomDataStorage source, @NotNull CustomDataStorage target, @NotNull LoggerWrapper logger) throws Exception {
        for (var key : source.getKeys()) {
            var data = source.load(key.namespace(), key.key());
            target.save(key.namespace(), key.key(), data);

            if (logger.debug()) {
                logger.info("Migrated custom data (" + key + "): " + data);
            }
        }

        return this.itemMigratorResult;
    }
}
