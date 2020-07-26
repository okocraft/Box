package net.okocraft.box.plugin.listener;

import net.okocraft.box.plugin.Box;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractListener implements Listener {

    protected final Box plugin;

    public AbstractListener(@NotNull Box plugin) {
        this.plugin = plugin;
    }

    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void shutdown() {
        HandlerList.unregisterAll(this);
    }
}
