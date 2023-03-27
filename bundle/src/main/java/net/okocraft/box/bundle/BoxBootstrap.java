package net.okocraft.box.bundle;

import net.okocraft.box.core.BoxPlugin;
import net.okocraft.box.storage.api.registry.StaticStorageRegistry;
import net.okocraft.box.storage.migrator.StorageMigrator;
import net.okocraft.box.storage.migrator.config.MigrationConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;

public final class BoxBootstrap extends JavaPlugin {

    private BoxPlugin boxPlugin;
    private boolean isPaper;
    private boolean isLoaded;

    public BoxBootstrap() {
        try {
            Bukkit.class.getMethod("getMinecraftVersion");
            isPaper = true;
        } catch (NoSuchMethodException e) {
            getLogger().severe("Box only supports Paper or its fork.");
            getLogger().severe("Please change your Spigot server to Paper to use Box.");
            isPaper = false;
            return;
        }

        this.boxPlugin = new BoxPlugin(this, getFile().toPath());

        Bundled.storageMap().forEach(StaticStorageRegistry::register);
    }

    @Override
    public void onLoad() {
        if (isPaper) {
            isLoaded = boxPlugin.load();
        }
    }

    @Override
    public void onEnable() {
        if (!isPaper) {
            getLogger().severe("Box only supports Paper or its fork.");
            getLogger().severe("Please change your Spigot server to Paper to use Box.");
            getLogger().severe("");
            getLogger().severe("Disabling box...");

            disablePlugin();
            return;
        }

        if (!isLoaded) {
            disablePlugin(); // An exception occurred while loading Box.
            return;
        }

        StorageMigrator migrator = null;

        try (var migrationYaml = MigrationConfigLoader.load(boxPlugin.getPluginDirectory().resolve("migration.yml"), getLogger())) {
            if (MigrationConfigLoader.isMigrationRequested(migrationYaml, getLogger())) {
                migrator = MigrationConfigLoader.prepare(migrationYaml, getLogger());
            }
        }

        if (migrator != null) {
            try {
                runMigration(migrator);
                getLogger().info("Migration is complete.");
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "An exception occurred while migrating data.", e);
                disablePlugin();
                return;
            }
        }

        var startTime = Instant.now();

        if (!boxPlugin.enable()) {
            disablePlugin();
            return;
        }

        Bundled.features().forEach(boxPlugin::register);

        var timeTaken = Duration.between(startTime, Instant.now());
        getLogger().info("Successfully enabled! (" + timeTaken.toMillis() + "ms)");
    }

    @Override
    public void onDisable() {
        if (isPaper && isLoaded) {
            Bundled.features().forEach(boxPlugin::unregister);
            boxPlugin.disable();
            getLogger().info("Successfully disabled. Goodbye!");
        }
    }

    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }

    private void runMigration(@NotNull StorageMigrator migrator) throws Exception {
        getLogger().info("Initializing storages...");
        migrator.init();

        getLogger().info("Migrating data...");
        migrator.run();

        getLogger().info("Shutting down storages...");
        migrator.close();
    }
}
