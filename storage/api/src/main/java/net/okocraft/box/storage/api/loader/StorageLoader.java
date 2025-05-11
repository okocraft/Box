package net.okocraft.box.storage.api.loader;

import net.okocraft.box.storage.api.exporter.BoxDataFile;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.version.StorageVersion;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

import static net.okocraft.box.api.util.BoxLogger.logger;

public final class StorageLoader {

    public static boolean initialize(@NotNull Storage storage, @NotNull String initialDataFilepath) {
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

        if (!storage.isFirstStartup() && storageVersion.isBefore(StorageVersion.latest())) {
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

        if (storage.isFirstStartup()) {
            if (!initialDataFilepath.isEmpty()) {
                logger().info("Importing Box data from {}", initialDataFilepath);
                try {
                    importFromInitialDataFile(storage, initialDataFilepath);
                } catch (Exception e) {
                    logger().error("Failed to import initial data.", e);
                    return false;
                }
            }
            return true;
        }

        try {
            storage.getCustomDataStorage().updateFormatIfNeeded(); // Update data format on database
        } catch (Exception e) {
            logger().error("Failed to update the format of custom data.", e);
            return false;
        }

        return true;
    }

    private static void importFromInitialDataFile(@NotNull Storage storage, @NotNull String initialDataFilepath) throws Exception {
        var filepath = Path.of(initialDataFilepath);
        if (!Files.isRegularFile(filepath)) {
            return;
        }

        var decodeResult = BoxDataFile.decode(filepath);
        if (decodeResult.isFailure()) {
            throw new RuntimeException(decodeResult.toString());
        }

        var data = decodeResult.unwrap();

        storage.saveDataVersion(data.dataVersion());

        logger().info("Importing {} users...", data.users().size());
        storage.getUserStorage().saveBoxUsers(data.users());

        logger().info("Importing {} default items...", data.defaultItems().size());
        storage.defaultItemStorage().saveDefaultItems(data.defaultItems());

        logger().info("Importing {} custom items...", data.customItems().size());
        storage.customItemStorage().saveCustomItems(data.customItems());

        logger().info("Importing {} stock holders...", data.stockHolders().size());
        storage.getStockStorage().saveAllStockData(data.stockHolders());

        logger().info("Importing {} custom data...", data.customData().size());
        storage.getCustomDataStorage().saveAllData(data.customData());
    }

    private StorageLoader() {
        throw new UnsupportedOperationException();
    }
}
