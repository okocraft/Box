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

public final class StaticStorageRegistry {

    private static final Map<String, Function<Configuration, Storage>> STORAGE_MAP = new HashMap<>();

    public static void register(@NotNull String name, @NotNull Function<Configuration, Storage> storageFunction) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(storageFunction);
        STORAGE_MAP.put(name.toLowerCase(Locale.ENGLISH), storageFunction);
    }

    public static void unregister(@NotNull String name) {
        Objects.requireNonNull(name);
        STORAGE_MAP.remove(name.toLowerCase(Locale.ENGLISH));
    }

    public static @Nullable Function<Configuration, Storage>  getStorageFunction(@NotNull String name) {
        return STORAGE_MAP.get(name.toLowerCase(Locale.ENGLISH));
    }

    public static @NotNull Function<Configuration, Storage>  getYamlStorageSupplier() {
        var function = getStorageFunction("yaml");

        if (function != null) {
            return function;
        } else {
            throw new IllegalStateException("Where is the yaml storage!?");
        }
    }
}
