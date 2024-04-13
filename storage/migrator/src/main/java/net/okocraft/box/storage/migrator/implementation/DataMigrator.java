package net.okocraft.box.storage.migrator.implementation;

import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface DataMigrator<R> {

    interface Base<T, R> {

        @NotNull DataMigrator<R> createMigrator(T previousResult);

        default boolean checkRequirements(@NotNull Storage source, @NotNull Storage target) throws Exception {
            return true;
        }

        default <R2> @NotNull Base<T, R2> next(@NotNull Base<R, R2> next) {
            return new Base<>() {
                @Override
                public @NotNull DataMigrator<R2> createMigrator(@NotNull T previousResult) {
                    return (source, target, debug) -> next.createMigrator(
                            Base.this.createMigrator(previousResult).migrate(source, target, debug)
                    ).migrate(source, target, debug);
                }

                @Override
                public boolean checkRequirements(@NotNull Storage source, @NotNull Storage target) throws Exception {
                    return Base.this.checkRequirements(source, target) && next.checkRequirements(source, target);
                }
            };
        }
    }

    @NotNull R migrate(@NotNull Storage source, @NotNull Storage target, boolean debug) throws Exception;

    default <R2> @NotNull DataMigrator<R2> next(@NotNull Function<R, DataMigrator<R2>> nextMigrator) {
        return (source, target, debug) -> nextMigrator.apply(this.migrate(source, target, debug)).migrate(source, target, debug);
    }
}
