package net.okocraft.box.plugin;

import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.okocraft.box.api.APISetter;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.bootstrap.BoxBootstrapContext;
import net.okocraft.box.core.BoxCore;
import net.okocraft.box.core.PluginContext;
import net.okocraft.box.core.config.Config;
import net.okocraft.box.datafixer.AutoStoreSettingDataFixer;
import net.okocraft.box.platform.PlatformDependent;
import net.okocraft.box.storage.api.holder.StorageHolder;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import net.okocraft.box.storage.migrator.config.MigrationConfigLoader;
import net.okocraft.box.storage.migrator.implementation.CustomDataMigrator;
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

    public BoxPlugin(@NotNull BoxBootstrapContext boxBootstrapContext) {
        PluginContext context;

        try {
            context = new PluginContext(
                    this,
                    boxBootstrapContext.getVersion(),
                    boxBootstrapContext.getPluginDirectory(),
                    PlatformDependent.createScheduler(this),
                    boxBootstrapContext.getEventServiceProvider(),
                    boxBootstrapContext.createMessageProvider(),
                    new Config(boxBootstrapContext.getPluginDirectory()),
                    PlatformDependent.createItemProvider(),
                    PlatformDependent.createCommandRegisterer(this.getName().toLowerCase(Locale.ENGLISH))
            );
        } catch (PlatformDependent.NotSupportedException e) {
            this.pluginContext = null;
            this.boxCore = null;
            this.storageRegistry = null;
            this.features = null;
            this.status = Status.UNSUPPORTED_PLATFORM;
            BoxLogger.logger().error(e.reason);
            return;
        }

        this.pluginContext = context;
        this.boxCore = new BoxCore(context);
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

        try {
            StorageHolder.init(this.pluginContext.config().loadAndCreateStorage(this.storageRegistry));
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
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            runMigratorIfNeeded();
        } catch (Exception e) {
            BoxLogger.logger().error("An exception occurred while migrating data.", e);
            this.status = Status.EXCEPTION_OCCURRED;
            return;
        }

        var start = Instant.now();

        if (!this.boxCore.enable(StorageHolder.getStorage())) {
            this.status = Status.EXCEPTION_OCCURRED;
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        APISetter.set(this.boxCore);

        try {
            this.boxCore.initializeFeatures(this.features);
        } catch (IllegalStateException e) {
            BoxLogger.logger().error("An exception occurred while initializing features", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.features.clear();

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
        APISetter.unset();

        this.boxCore.disable();
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

        var migrator = MigrationConfigLoader.prepare(loadedMigrationSetting, this.storageRegistry, this.pluginContext.dataDirectory(), this.pluginContext.defaultItemProvider());

        if (migrator != null) {
            var start = Instant.now();

            BoxLogger.logger().info("Initializing storages...");
            migrator.init();
            CustomDataMigrator.addDataFixer(new AutoStoreSettingDataFixer());

            BoxLogger.logger().info("Migrating data...");
            migrator.run();

            BoxLogger.logger().info("Shutting down storages...");
            migrator.close();

            var finish = Instant.now();
            BoxLogger.logger().info("Migration is completed. ({}ms)", Duration.between(start, finish).toMillis());
        }
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
