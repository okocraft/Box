package net.okocraft.box.storage.migrator.implementation;

import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNull;

abstract class AbstractDataMigrator<R, ST> implements DataMigrator<R> {

    @Override
    public @NotNull R migrate(@NotNull Storage source, @NotNull Storage target, boolean debug) throws Exception {
        BoxLogger.logger().info("Starting {}...", this.getClass().getSimpleName());
        return this.migrateData(this.getDataStorage(source), this.getDataStorage(target), debug);
    }

    protected abstract ST getDataStorage(@NotNull Storage storage);

    protected abstract @NotNull R migrateData(@NotNull ST source, @NotNull ST target, boolean debug) throws Exception;

}
