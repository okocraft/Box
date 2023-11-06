package net.okocraft.box.storage.migrator.implementation;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.migrator.util.LoggerWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface DataMigrator<R> {

    @NotNull R migrate(@NotNull Storage source, @NotNull Storage target, @NotNull LoggerWrapper logger) throws Exception;

    default <R2> @NotNull DataMigrator<R2> next(@NotNull Function<R, DataMigrator<R2>> nextMigrator) {
        return (source, target, logger) -> nextMigrator.apply(this.migrate(source, target, logger)).migrate(source, target, logger);
    }
}
