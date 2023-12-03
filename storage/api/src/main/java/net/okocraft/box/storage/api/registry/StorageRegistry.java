package net.okocraft.box.storage.api.registry;

import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class StorageRegistry {

    private final Map<String, StorageType<?>> storageMap = new HashMap<>();
    private String defaultStorageName;

    public <R extends Record> void register(@NotNull String name, @NotNull Class<R> settingClass, @NotNull Function<StorageContext<R>, Storage> storageFunction) {
        this.storageMap.put(name.toLowerCase(Locale.ENGLISH), new StorageType<>(name, settingClass, storageFunction));
    }

    public @Nullable StorageType<?> getOrNull(@NotNull String name) {
        return this.storageMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public @NotNull StorageType<?> getDefault() {
        return this.storageMap.get(this.getDefaultStorageName());
    }

    public @NotNull @UnmodifiableView Collection<StorageType<?>> getEntries() {
        return Collections.unmodifiableCollection(this.storageMap.values());
    }

    public void setDefaultStorageName(@NotNull String name) {
        if (!this.storageMap.containsKey(name.toLowerCase(Locale.ENGLISH))) {
            throw new IllegalArgumentException("The storage type '" + name + "' is not registered.");
        }

        this.defaultStorageName = name.toLowerCase(Locale.ENGLISH);
    }

    public @NotNull String getDefaultStorageName() {
        return this.defaultStorageName;
    }

}
