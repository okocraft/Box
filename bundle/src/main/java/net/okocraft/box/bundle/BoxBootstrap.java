package net.okocraft.box.bundle;

import net.okocraft.box.core.BoxPlugin;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;

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

        if (!isLoaded) {
            disablePlugin(); // An exception occurred while loading Box.
            return;
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
}
