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
        source.visitAllData((key, mapNode) -> {
            try {
                target.saveData(key, mapNode);
            } catch (Exception e) {
                sneakyThrow(e);
            }

            if (logger.debug()) {
                logger.info("Migrated custom data (" + key + "): " + mapNode);
            }
        });
        return this.itemMigratorResult;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(@NotNull Throwable exception) throws T {
        throw (T) exception;
    }
}
