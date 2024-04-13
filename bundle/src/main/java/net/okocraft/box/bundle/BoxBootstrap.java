package net.okocraft.box.bundle;

import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.core.BoxPlugin;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import net.okocraft.box.storage.migrator.StorageMigrator;
import net.okocraft.box.storage.migrator.config.MigrationConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.SubstituteLogger;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;

public final class BoxBootstrap extends JavaPlugin {

    private BoxPlugin boxPlugin;
    private boolean isPaper;
    private boolean isSupportedVersion;
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

        this.isSupportedVersion = MCDataVersion.current().isBeforeOrSame(MCDataVersion.MC_1_20_4);

        ((SubstituteLogger) BoxLogger.logger()).setDelegate(this.getComponentLogger());
        this.boxPlugin = new BoxPlugin(this, getFile().toPath());

        Bundled.storageMap().forEach(StorageRegistry::register);
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

        if (!isSupportedVersion) {
            getLogger().severe("Box v5.x.x is supported up to Minecraft 1.20.4.");
            getLogger().severe("If you want to use Minecraft 1.20.5 or later, please update to v6.x.x.");
            getLogger().severe("");
            getLogger().severe("Current Minecraft version: " + Bukkit.getMinecraftVersion());
            getLogger().severe("Current Box version: " + this.boxPlugin.getPluginVersion());
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

        var start = Instant.now();

        if (!boxPlugin.enable()) {
            disablePlugin();
            return;
        }

        Bundled.features().forEach(boxPlugin::register);

        var finish = Instant.now();
        BoxLogger.logger().info("Successfully enabled! ({}ms)", Duration.between(start, finish).toMillis());
    }

    @Override
    public void onDisable() {
        if (isPaper && isSupportedVersion && isLoaded) {
            Bundled.features().forEach(boxPlugin::unregister);
            boxPlugin.disable();
            BoxLogger.logger().info("Successfully disabled. Goodbye!");
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
