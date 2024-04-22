package net.okocraft.box.plugin;

import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.okocraft.box.api.APISetter;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.bootstrap.BoxBootstrapContext;
import net.okocraft.box.core.BoxCore;
import net.okocraft.box.core.PluginContext;
import net.okocraft.box.core.config.Config;
import net.okocraft.box.platform.PlatformDependent;
import net.okocraft.box.storage.api.holder.StorageHolder;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import net.okocraft.box.storage.migrator.config.MigrationConfigLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

public final class BoxPlugin extends JavaPlugin {

    private final BoxCore boxCore;
    private final PluginContext pluginContext;
    private final StorageRegistry storageRegistry;
    private final List<BoxFeature> features;

    private Status status = Status.NOT_LOADED;
    private Storage storage;

    public BoxPlugin(@NotNull BoxBootstrapContext boxBootstrapContext) {
        try {
            PlatformDependent.checkVersionRequirement();
        } catch (PlatformDependent.NotSupportedException e) {
            this.pluginContext = null;
            this.boxCore = null;
            this.storageRegistry = null;
            this.features = null;
            this.status = Status.UNSUPPORTED_PLATFORM;
            BoxLogger.logger().error(e.reason);
            return;
        }

        this.pluginContext = new PluginContext(
                this,
                boxBootstrapContext.getVersion(),
                boxBootstrapContext.getDataDirectory(),
                PlatformDependent.createScheduler(this),
                boxBootstrapContext.getEventManager(),
                boxBootstrapContext.createMessageProvider(),
                new Config(boxBootstrapContext.getDataDirectory()),
                PlatformDependent.createItemProvider(),
                PlatformDependent.createCommandRegisterer(this.getName().toLowerCase(Locale.ENGLISH))
        );

        this.boxCore = new BoxCore(this.pluginContext);
        this.storageRegistry = boxBootstrapContext.getStorageRegistry();
        this.features = boxBootstrapContext.getBoxFeatureList();
    }

    @Override
    public void onLoad() {
        if (this.status != Status.NOT_LOADED) {
            BoxLogger.logger().error("Cannot load Box ({})", this.status);
            return;
        }

        var start = Instant.now();

        this.pluginContext.eventManager().initializeAsyncEventCaller(this.pluginContext.scheduler());

        try {
            this.storage = this.pluginContext.config().loadAndCreateStorage(this.storageRegistry);
        } catch (IOException e) {
            BoxLogger.logger().error("Could not load config.yml", e);
            this.status = Status.EXCEPTION_OCCURRED;
            return;
        }

        try {
            this.pluginContext.messageProvider().load();
        } catch (IOException e) {
            BoxLogger.logger().error("Could not load messages.", e);
            this.status = Status.EXCEPTION_OCCURRED;
            this.unload();
            return;
        }

        this.status = Status.LOADED;
        var finish = Instant.now();

        BoxLogger.logger().info("Successfully loaded! ({}ms)", Duration.between(start, finish).toMillis());
    }

    @Override
    public void onEnable() {
        if (this.status != Status.LOADED) {
            BoxLogger.logger().error("Cannot enable Box ({})", this.status);
            return;
        }

        try {
            runMigratorIfNeeded();
        } catch (Exception e) {
            BoxLogger.logger().error("An exception occurred while migrating data.", e);
            this.status = Status.EXCEPTION_OCCURRED;
            this.unload();
            return;
        }

        var start = Instant.now();

        if (!this.boxCore.enable(this.storage)) {
            this.status = Status.EXCEPTION_OCCURRED;
            this.unload();
            return;
        }

        APISetter.set(this.boxCore);
        StorageHolder.init(this.storage);

        try {
            this.boxCore.initializeFeatures(this.features);
        } catch (IllegalStateException e) {
            BoxLogger.logger().error("An exception occurred while initializing features", e);
            this.unload();
            return;
        } finally {
            this.features.clear();
        }

        this.status = Status.ENABLED;

        var finish = Instant.now();
        BoxLogger.logger().info("Successfully enabled! ({}ms)", Duration.between(start, finish).toMillis());
    }

    @Override
    public void onDisable() {
        if (this.status != Status.ENABLED) {
            BoxLogger.logger().error("Cannot disable Box ({})", this.status);
            return;
        }

        this.boxCore.disableAllFeatures();
        this.boxCore.disable();

        this.unload();

        this.status = Status.DISABLED;

        BoxLogger.logger().info("Successfully disabled. Goodbye!");
    }

    private void runMigratorIfNeeded() throws Exception {
        var filepath = this.pluginContext.dataDirectory().resolve("migration.yml");

        if (!Files.isRegularFile(filepath)) {
            return;
        }

        var loadedMigrationSetting = YamlFormat.COMMENT_PROCESSING.load(filepath);

        if (loadedMigrationSetting.getBoolean("migration-mode")) {
            loadedMigrationSetting.set("migration-mode", false);
            YamlFormat.COMMENT_PROCESSING.save(loadedMigrationSetting, filepath);
        } else {
            return;
        }

        try (var migrator = MigrationConfigLoader.prepare(loadedMigrationSetting, this.storageRegistry, this.pluginContext.dataDirectory(), this.pluginContext.defaultItemProvider())) {
            if (migrator != null) {
                var start = Instant.now();

                BoxLogger.logger().info("Initializing storages...");
                migrator.init();

                BoxLogger.logger().info("Migrating data...");
                migrator.run();

                BoxLogger.logger().info("Shutting down storages...");
                migrator.close();

                var finish = Instant.now();
                BoxLogger.logger().info("Migration is completed. ({}ms)", Duration.between(start, finish).toMillis());
            }
        }
    }

    private void unload() { // This method is for releasing resources correctly in any state.
        if (BoxAPI.isLoaded()) {
            APISetter.unset();
        }

        if (this.storage != null) {
            BoxLogger.logger().info("Closing the storage...");

            if (StorageHolder.isInitialized()) {
                StorageHolder.unset();
            }

            try {
                this.storage.close();
            } catch (Exception e) {
                BoxLogger.logger().error("Could not close the storage.", e);
            } finally {
                this.storage = null;
            }
        }

        BoxLogger.logger().info("Unloading messages...");
        this.pluginContext.messageProvider().unload();
    }

    public enum Status {
        NOT_LOADED,
        LOADED,
        ENABLED,
        DISABLED,
        EXCEPTION_OCCURRED,
        UNSUPPORTED_PLATFORM
    }
}
