package net.okocraft.box.core.model.data;

import com.github.siroshun09.configapi.api.Configuration;
import net.okocraft.box.api.model.data.CustomDataContainer;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BoxCustomDataContainer implements CustomDataContainer {

    private final CustomDataStorage customDataStorage;
    private final Executor executor;

    public BoxCustomDataContainer(@NotNull CustomDataStorage customDataStorage, @NotNull Executor executor) {
        this.customDataStorage = customDataStorage;
        this.executor = executor;
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Configuration> get(@NotNull String namespace, @NotNull String key) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(key);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return customDataStorage.load(namespace, key);
            } catch (Exception e) {
                throw new RuntimeException("Could not load custom data (" + namespace + ", " + key + ")", e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> set(@NotNull String namespace, @NotNull String key, @NotNull Configuration configuration) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(key);
        Objects.requireNonNull(configuration);

        return CompletableFuture.runAsync(() -> {
            try {
                customDataStorage.save(namespace, key, configuration);
            } catch (Exception e) {
                throw new RuntimeException("Could not load custom data (" + namespace + ", " + key + ")", e);
            }
        }, executor);
    }
}
