package net.okocraft.box.storage.api.loader;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.version.StorageVersion;
import org.jetbrains.annotations.NotNull;

import static net.okocraft.box.api.util.BoxLogger.logger;

public final class StorageLoader {

    public static boolean initialize(@NotNull Storage storage) {
        try {
            storage.init();
        } catch (Exception e) {
            logger().error("Failed to initialize storage.", e);
            return false;
        }

        StorageVersion storageVersion;

        try {
            storageVersion = storage.getStorageVersion();
        } catch (Exception e) {
            logger().error("Failed to get the version of the storage.", e);
            return false;
        }

        if (storageVersion.isAfter(StorageVersion.latest())) {
            logger().error("Downgrading the storage version is not supported.");
            return false;
        }


        if (storageVersion.isBefore(StorageVersion.latest())) {
            try {
                storage.applyStoragePatches(storageVersion, StorageVersion.latest());
            } catch (Exception e) {
                logger().error("Failed to patch the storage. current: {} latest: {}", storage, StorageVersion.latest(), e);
                return false;
            }
        }

        try {
            storage.saveStorageVersion(StorageVersion.latest());
        } catch (Exception e) {
            logger().error("Failed to save the storage version ({})", StorageVersion.latest(), e);
            return false;
        }

        try {
            storage.prepare();
        } catch (Exception e) {
            logger().error("Failed to prepare the storage.", e);
            return false;
        }

        try {
            storage.getCustomDataStorage().updateFormatIfNeeded(); // Update data format on database
        } catch (Exception e) {
            logger().error("Failed to update the format of custom data.", e);
            return false;
        }

        return true;
    }

    private StorageLoader() {
        throw new UnsupportedOperationException();
    }
}
