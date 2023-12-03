package net.okocraft.box.plugin;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.bootstrap.BoxBootstrapContext;
import net.okocraft.box.core.BoxCore;
import net.okocraft.box.core.PluginContext;
import net.okocraft.box.core.config.Config;
import net.okocraft.box.platform.PlatformDependent;
import net.okocraft.box.storage.api.holder.StorageHolder;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import net.okocraft.box.storage.api.registry.BaseStorageContext;
import net.okocraft.box.storage.migrator.config.MigrationConfigLoader;
import net.okocraft.box.util.TranslationDirectoryUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.Level;

public final class BoxPlugin extends JavaPlugin {

    private final BoxCore boxCore;
    private final BaseStorageContext storageContext;
    private final PluginContext pluginContext;
    private final StorageRegistry storageRegistry;
    private final @NotNull List<Supplier<? extends BoxFeature>> preregisteredFeatures;

    private Status status = Status.NOT_LOADED;

    public BoxPlugin(@NotNull BoxBootstrapContext boxBootstrapContext) {
        this.storageContext = new BaseStorageContext(
                boxBootstrapContext.getPluginDirectory(),
                this.getLogger()
        );

        this.pluginContext = new PluginContext(
                this,
                boxBootstrapContext.getVersion(),
                boxBootstrapContext.getPluginDirectory(),
                boxBootstrapContext.getJarFile(),
                PlatformDependent.createScheduler(this),
                boxBootstrapContext.getEventBus(),
                new Config(boxBootstrapContext.getPluginDirectory().resolve("config.yml")),
                TranslationDirectoryUtil.fromContext(boxBootstrapContext),
                PlatformDependent.createItemProvider(),
                PlatformDependent.createCommandRegisterer(this.getName().toLowerCase(Locale.ENGLISH))
        );
        this.boxCore = new BoxCore(pluginContext);
        this.storageRegistry = boxBootstrapContext.getStorageRegistry();
        this.preregisteredFeatures = boxBootstrapContext.getBoxFeatureList();
    }

    @Override
    public void onLoad() {
        if (status != Status.NOT_LOADED) {
            return;
        }

        var start = Instant.now();

        try {
            StorageHolder.init(this.pluginContext.config().loadAndCreateStorage(this.storageRegistry, this.storageContext));
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not load config.yml", e);
            this.status = Status.EXCEPTION_OCCURRED;
            return;
        }

        this.status = Status.LOADED;
        var finish = Instant.now();

        getLogger().info("Successfully loaded! (" + Duration.between(start, finish).toMillis() + "ms)");
    }

    @Override
    public void onEnable() {
        if (status != Status.LOADED) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            runMigratorIfNeeded();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An exception occurred while migrating data.", e);
            status = Status.EXCEPTION_OCCURRED;
            return;
        }

        var start = Instant.now();

        if (boxCore.enable(StorageHolder.getStorage())) {
            status = Status.ENABLED;
        } else {
            status = Status.EXCEPTION_OCCURRED;
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        for (var featureSupplier : this.preregisteredFeatures) {
            this.boxCore.register(featureSupplier.get());
        }

        this.preregisteredFeatures.clear();

        var finish = Instant.now();
        getLogger().info("Successfully enabled! (" + Duration.between(start, finish).toMillis() + "ms)");
    }

    @Override
    public void onDisable() {
        if (status == Status.ENABLED) {
            boxCore.disable();
            status = Status.DISABLED;
        }

        getLogger().info("Successfully disabled. Goodbye!");
    }

    private void runMigratorIfNeeded() throws Exception {
        var filepath = this.pluginContext.dataDirectory().resolve("migration.yml");

        if (!Files.isRegularFile(filepath)) {
            return;
        }

        MapNode loadedMigrationSetting;

        try (var reader = Files.newBufferedReader(filepath, StandardCharsets.UTF_8)) {
            loadedMigrationSetting = YamlFormat.COMMENT_PROCESSING.load(reader);
        }

        if (loadedMigrationSetting.getBoolean("migration-mode")) {
            loadedMigrationSetting.set("migration-mode", false);

            try (var writer = Files.newBufferedWriter(filepath, StandardCharsets.UTF_8)) {
                YamlFormat.COMMENT_PROCESSING.save(loadedMigrationSetting, writer);
            }
        } else {
            return;
        }

        var migrator = MigrationConfigLoader.prepare(loadedMigrationSetting, this.storageRegistry, this.pluginContext.defaultItemProvider(), this.storageContext);

        if (migrator != null) {
            var start = Instant.now();

            getLogger().info("Initializing storages...");
            migrator.init();

            getLogger().info("Migrating data...");
            migrator.run();

            getLogger().info("Shutting down storages...");
            migrator.close();

            var finish = Instant.now();
            getLogger().info("Migration is completed. (" + Duration.between(start, finish).toMillis() + "ms)");
        }
    }

    public enum Status {
        NOT_LOADED,
        LOADED,
        ENABLED,
        DISABLED,
        EXCEPTION_OCCURRED
    }
}
