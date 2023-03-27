package net.okocraft.box.storage.migrator.config;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.registry.StaticStorageRegistry;
import net.okocraft.box.storage.migrator.StorageMigrator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MigrationConfigLoader {

    public static @Nullable YamlConfiguration load(@NotNull Path path, @NotNull Logger logger) {
        if (Files.isRegularFile(path)) {
            var yaml = YamlConfiguration.create(path);
            try {
                yaml.load();
                return yaml;
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not load " + path.getFileName().toString(), e);
            }
        }

        return null;
    }

    public static boolean isMigrationRequested(@Nullable YamlConfiguration config, @NotNull Logger logger) {
        if (config == null || !config.isLoaded() || !config.getBoolean("migration-mode")) {
            return false;
        }

        try {
            config.set("migration-mode", false);
            config.save();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not save " + config.getPath().getFileName().toString(), e);
        }

        return true;
    }

    public static @Nullable StorageMigrator prepare(@NotNull Configuration config, @NotNull Logger logger) {
        var sourceStorageSetting = config.getOrCreateSection("source-storage");
        var targetStorageSetting = config.getOrCreateSection("target-storage");

        var sourceStorageFunction = StaticStorageRegistry.getStorageFunction(sourceStorageSetting.getString("type"));
        var targetStorageFunction = StaticStorageRegistry.getStorageFunction(targetStorageSetting.getString("type"));

        if (sourceStorageFunction == null) {
            logger.warning("Invalid storage type (source): " + sourceStorageSetting.getString("type", "not set"));
            return null;
        }

        if (targetStorageFunction == null) {
            logger.warning("Invalid storage type (target): " + targetStorageSetting.getString("type", "not set"));
            return null;
        }

        var sourceStorage = sourceStorageFunction.apply(sourceStorageSetting);
        var targetStorage = targetStorageFunction.apply(targetStorageSetting);

        printMigrationInfo(sourceStorage, targetStorage, logger);

        if (config.getBoolean("debug")) {
            logger.info("Perform migration as debug mode");
            StorageMigrator.debug = true;
        }

        return new StorageMigrator(sourceStorage, targetStorage, logger);
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
