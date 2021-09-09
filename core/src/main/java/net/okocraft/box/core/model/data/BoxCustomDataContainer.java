package net.okocraft.box.core.model.data;

import com.github.siroshun09.configapi.api.Configuration;
import net.okocraft.box.api.model.data.CustomDataContainer;
import net.okocraft.box.core.storage.model.data.CustomDataStorage;
import net.okocraft.box.core.util.InternalExecutors;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BoxCustomDataContainer implements CustomDataContainer {

    private final CustomDataStorage customDataStorage;
    private final ExecutorService executor;

    public BoxCustomDataContainer(@NotNull CustomDataStorage customDataStorage) {
        this.customDataStorage = customDataStorage;
        this.executor = InternalExecutors.newSingleThreadExecutor("Custom-Data");
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Configuration> get(@NotNull String namespace, @NotNull String key) {
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
        return CompletableFuture.runAsync(() -> {
            try {
                customDataStorage.save(namespace, key, configuration);
            } catch (Exception e) {
                throw new RuntimeException("Could not load custom data (" + namespace + ", " + key + ")", e);
            }
        }, executor);
    }
}
