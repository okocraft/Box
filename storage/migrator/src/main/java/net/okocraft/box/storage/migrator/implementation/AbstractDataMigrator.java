package net.okocraft.box.storage.migrator.implementation;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.migrator.util.LoggerWrapper;
import org.jetbrains.annotations.NotNull;

abstract class AbstractDataMigrator<R, ST> implements DataMigrator<R> {

    @Override
    public @NotNull R migrate(@NotNull Storage source, @NotNull Storage target, @NotNull LoggerWrapper logger) throws Exception {
        logger.info("Starting " + getClass().getSimpleName() + "...");
        return this.migrateData(this.getDataStorage(source), this.getDataStorage(target), logger);
    }

    protected abstract ST getDataStorage(@NotNull Storage storage);

    protected abstract @NotNull R migrateData(@NotNull ST source, @NotNull ST target, @NotNull LoggerWrapper logger) throws Exception;

}
