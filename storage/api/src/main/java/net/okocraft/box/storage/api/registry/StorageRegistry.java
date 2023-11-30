package net.okocraft.box.storage.api.registry;

import com.github.siroshun09.configapi.api.Configuration;
import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class StorageRegistry {

    private final Map<String, Function<Configuration, Storage>> storageMap = new HashMap<>();
    private String defaultStorageName;

    public void register(@NotNull String name, @NotNull Function<Configuration, Storage> storageFunction) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(storageFunction);

        storageMap.put(name.toLowerCase(Locale.ENGLISH), storageFunction);
    }

    public void unregister(@NotNull String name) {
        Objects.requireNonNull(name);

        if (defaultStorageName != null && defaultStorageName.equals(name)) {
            throw new IllegalArgumentException("Could not unregister the default storage type.");
        }

        storageMap.remove(name.toLowerCase(Locale.ENGLISH));
    }

    public @Nullable Function<Configuration, Storage> getStorageFunction(@NotNull String name) {
        return storageMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public void setDefaultStorageName(@NotNull String name) {
        if (!storageMap.containsKey(name.toLowerCase(Locale.ENGLISH))) {
            throw new IllegalArgumentException("The storage type '" + name + "' is not registered.");
        }

        this.defaultStorageName = name.toLowerCase(Locale.ENGLISH);
    }

    public @NotNull String getDefaultStorageName() {
        return defaultStorageName;
    }

    public @NotNull Storage createDefaultStorage(@NotNull Configuration config) {
        return Objects.requireNonNull(getStorageFunction(this.defaultStorageName)).apply(config);
    }
}
