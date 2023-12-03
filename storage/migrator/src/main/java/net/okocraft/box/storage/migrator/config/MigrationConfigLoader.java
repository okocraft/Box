package net.okocraft.box.storage.migrator.config;

import com.github.siroshun09.configapi.core.node.MapNode;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import net.okocraft.box.storage.api.registry.BaseStorageContext;
import net.okocraft.box.storage.api.util.item.DefaultItemProvider;
import net.okocraft.box.storage.migrator.StorageMigrator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.logging.Logger;

public final class MigrationConfigLoader {

    public static @Nullable StorageMigrator prepare(@NotNull MapNode config, @NotNull StorageRegistry storageRegistry,
                                                    @NotNull DefaultItemProvider defaultItemProvider, @NotNull BaseStorageContext storageContext) {
        var sourceStorageSetting = config.getMap("source-storage");
        var targetStorageSetting = config.getMap("target-storage");

        var sourceStorageType = sourceStorageSetting.getString("type", "not set");
        var targetStorageType = targetStorageSetting.getString("type", "not set");

        var sourceStorageEntry = storageRegistry.getOrNull(sourceStorageType);
        var targetStorageEntry = storageRegistry.getOrNull(targetStorageType);

        var logger = storageContext.logger();

        if (sourceStorageEntry == null) {
            logger.warning("Invalid storage type (source): " + sourceStorageType);
            return null;
        }

        if (targetStorageEntry == null) {
            logger.warning("Invalid storage type (target): " + targetStorageType);
            return null;
        }

        var sourceStorage = sourceStorageEntry.create(storageContext, sourceStorageSetting.getMap(sourceStorageType.toLowerCase(Locale.ENGLISH)));
        var targetStorage = targetStorageEntry.create(storageContext, targetStorageSetting.getMap(targetStorageType.toLowerCase(Locale.ENGLISH)));

        printMigrationInfo(sourceStorage, targetStorage, logger);

        boolean debug = config.getBoolean("debug");

        if (debug) {
            logger.info("Perform migration as debug mode");
        }

        return new StorageMigrator(sourceStorage, targetStorage, defaultItemProvider, logger, debug);
    }

    private static void printMigrationInfo(@NotNull Storage source, @NotNull Storage target, @NotNull Logger logger) {
        logger.info("===== Migration Info =====");
        logger.info("Source: " + source.getName());
        logger.info("Source Storage Info:");
        printInfo(source, logger);
        logger.info("--------------------------");
        logger.info("Target: " + target.getName());
        logger.info("Target Storage Info:");
        printInfo(target, logger);
    }

    private static void printInfo(@NotNull Storage storage, @NotNull Logger logger) {
        storage.getInfo().stream().map(Storage.Property::asString).map(str -> str.indent(2)).map(String::trim).forEach(logger::info);
    }
}
