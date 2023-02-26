package net.okocraft.box.storage.migrator.data;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.migrator.util.LoggerWrapper;
import org.jetbrains.annotations.NotNull;

public interface DataMigrator<T> {

    @NotNull T getDataStorage(@NotNull Storage storage);

    void migrate(@NotNull T source, @NotNull T target, @NotNull LoggerWrapper logger) throws Exception;

}
