package net.okocraft.box.plugin.listener;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.listener.stick.BlockPlaceListener;
import net.okocraft.box.plugin.listener.stick.InteractListener;
import net.okocraft.box.plugin.listener.stick.ItemBreakListener;
import net.okocraft.box.plugin.listener.stick.ItemConsumeListener;
import net.okocraft.box.plugin.listener.stick.ProjectileLaunchListener;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class PluginListeners {

    private final Box plugin;

    private List<AbstractListener> listeners;

    public PluginListeners(@NotNull Box plugin) {
        this.plugin = plugin;
        this.listeners = new LinkedList<>();
    }

    public void register() {
        unregister();

        listeners.add(new PlayerConnectionListener(plugin));

        if (plugin.getGeneralConfig().isAutoStoreEnabled()) {
            listeners.add(new ItemPickupListener(plugin));
        }

        if (plugin.getGeneralConfig().isStickEnabled()) {
            listeners.addAll(List.of(
                    new BlockPlaceListener(plugin),
                    new InteractListener(plugin),
                    new ItemBreakListener(plugin),
                    new ItemConsumeListener(plugin),
                    new ProjectileLaunchListener(plugin)
            ));
        }

        listeners.forEach(AbstractListener::start);
    }

    public void unregister() {
        if (!listeners.isEmpty()) {
            listeners.forEach(AbstractListener::shutdown);
            listeners.clear();
        }
    }
}
