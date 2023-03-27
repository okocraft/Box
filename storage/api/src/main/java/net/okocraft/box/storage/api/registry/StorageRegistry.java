package net.okocraft.box.storage.api.registry;

import com.github.siroshun09.configapi.api.Configuration;
import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class StorageRegistry {

    private final Map<String, Function<Configuration, Storage>> storageMap = new HashMap<>();

    public void register(@NotNull String name, @NotNull Function<Configuration, Storage> storageFunction) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(storageFunction);

        storageMap.put(name.toLowerCase(Locale.ENGLISH), storageFunction);
    }

    public void unregister(@NotNull String name) {
        Objects.requireNonNull(name);
        storageMap.remove(name.toLowerCase(Locale.ENGLISH));
    }

    public @NotNull Function<Configuration, Storage> getStorageFunction(@NotNull String name) {
        return storageMap.get(name.toLowerCase(Locale.ENGLISH));
    }
}
