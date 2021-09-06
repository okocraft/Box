package net.okocraft.box.bundle;

import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.command.CommandFeature;
import net.okocraft.box.core.BoxPlugin;
import net.okocraft.box.stick.StickFeature;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class BoxBootstrap extends JavaPlugin {

    private final List<BoxFeature> bundledFeatures =
            List.of(new CommandFeature(), new StickFeature());

    private BoxPlugin boxPlugin;
    private boolean isPaper;
    private boolean isLoaded;

    public BoxBootstrap() {
        try {
            Bukkit.class.getMethod("getMinecraftVersion");
            isPaper = true;
        } catch (NoSuchMethodException e) {
            getLogger().severe("Box only supports Paper or its fork.");
            getLogger().severe("Please change your spigot server to Paper to use Box.");
            isPaper = false;
            return;
        }

        this.boxPlugin = new BoxPlugin(this, getFile().toPath());
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
            getLogger().severe("Please change your spigot server to Paper to use Box.");
            getLogger().severe("");
            getLogger().severe("Disabling box...");

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!isLoaded || !boxPlugin.enable()) {
            getServer().getPluginManager().disablePlugin(this);
        }

        bundledFeatures.forEach(boxPlugin::register);
    }

    @Override
    public void onDisable() {
        if (isPaper && isLoaded) {
            bundledFeatures.forEach(boxPlugin::unregister);
            boxPlugin.disable();
        }
    }
}
