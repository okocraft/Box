package net.okocraft.box.storage.migrator.data;

import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public interface DataMigrator<T> {

    @NotNull T getDataStorage(@NotNull Storage storage);

    void migrate(@NotNull T source, @NotNull T target, @NotNull Logger logger) throws Exception;

}
