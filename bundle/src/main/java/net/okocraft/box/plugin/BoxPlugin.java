package net.okocraft.box.plugin;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.bootstrap.BoxBootstrapContext;
import net.okocraft.box.platform.PlatformDependent;
import net.okocraft.box.core.BoxCore;
import net.okocraft.box.core.PluginContext;
import net.okocraft.box.storage.migrator.StorageMigrator;
import net.okocraft.box.storage.migrator.config.MigrationConfigLoader;
import net.okocraft.box.util.TranslationDirectoryUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.Level;

public final class BoxPlugin extends JavaPlugin {

    private final PluginContext pluginContext;
    private final BoxCore boxCore;
    private final @NotNull List<Supplier<? extends BoxFeature>> preregisteredFeatures;

    private Status status = Status.NOT_LOADED;

    public BoxPlugin(@NotNull BoxBootstrapContext boxBootstrapContext) {
        this.pluginContext = new PluginContext(
                this,
                boxBootstrapContext.getVersion(),
                boxBootstrapContext.getPluginDirectory(),
                boxBootstrapContext.getJarFile(),
                PlatformDependent.createScheduler(this),
                boxBootstrapContext.getEventBus(),
                YamlConfiguration.create(boxBootstrapContext.getPluginDirectory().resolve("config.yml")),
                TranslationDirectoryUtil.fromContext(boxBootstrapContext),
                boxBootstrapContext.getStorageRegistry(),
                PlatformDependent.createItemProvider(),
                PlatformDependent.createCommandRegisterer(this.getName().toLowerCase(Locale.ENGLISH))
        );
        this.boxCore = new BoxCore(pluginContext);
        this.preregisteredFeatures = boxBootstrapContext.getBoxFeatureList();
    }

    @Override
    public void onLoad() {
        if (status != Status.NOT_LOADED) {
            return;
        }

        var start = Instant.now();

        if (boxCore.load()) {
            status = Status.LOADED;
        } else {
            status = Status.EXCEPTION_OCCURRED;
            return;
        }

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

        if (boxCore.enable()) {
            status = Status.ENABLED;
        } else {
            status = Status.EXCEPTION_OCCURRED;
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        for (var featureSupplier : this.preregisteredFeatures) {
            this.boxCore.register(featureSupplier.get());
        }

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
        StorageMigrator migrator = null;

        try (var migrationYaml = MigrationConfigLoader.load(boxCore.getPluginDirectory().resolve("migration.yml"), getLogger())) {
            if (MigrationConfigLoader.isMigrationRequested(migrationYaml, getLogger())) {
                migrator = MigrationConfigLoader.prepare(migrationYaml, pluginContext.storageRegistry(), pluginContext.defaultItemProvider(), getLogger());
            }
        }

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
