package net.okocraft.box.storage.api.holder;

import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNull;

/**
 * A class to get currently used {@link net.okocraft.box.storage.api.model.Storage}
 */
public final class StorageHolder {

    private static Storage storage;

    public static void init(@NotNull Storage storage) {
        if (isInitialized()) {
            throw new IllegalStateException("StorageHolder is already initialized.");
        } else {
            StorageHolder.storage = storage;
        }
    }

    public static boolean isInitialized() {
        return StorageHolder.storage != null;
    }

    public static @NotNull Storage getStorage() {
        if (isInitialized()) {
            return StorageHolder.storage;
        } else {
            throw new IllegalStateException("StorageHolder is not initialized.");
        }
    }
}
