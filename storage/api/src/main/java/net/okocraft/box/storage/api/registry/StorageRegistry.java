package net.okocraft.box.storage.api.registry;

import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class StorageRegistry {

    private static final Map<String, Supplier<Storage>> STORAGE_MAP = new HashMap<>();

    public static void register(@NotNull String name, @NotNull Supplier<Storage> storageSupplier) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(storageSupplier);
        STORAGE_MAP.put(name.toLowerCase(Locale.ENGLISH), storageSupplier);
    }

    public static void unregister(@NotNull String name) {
        Objects.requireNonNull(name);
        STORAGE_MAP.remove(name.toLowerCase(Locale.ENGLISH));
    }

    public static @Nullable Supplier<Storage> getStorageSupplier(@NotNull String name) {
        return STORAGE_MAP.get(name.toLowerCase(Locale.ENGLISH));
    }

    public static @NotNull Supplier<Storage> getYamlStorageSupplier() {
        var supplier = getStorageSupplier("yaml");

        if (supplier != null) {
            return supplier;
        } else {
            throw new IllegalStateException("Where is the yaml storage!?");
        }
    }
}
