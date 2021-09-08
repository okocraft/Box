package net.okocraft.box.feature.stick;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.feature.stick.command.StickCommand;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import net.okocraft.box.feature.stick.listener.StickListener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class StickFeature extends AbstractBoxFeature {

    private final BoxStickItem boxStickItem = new BoxStickItem();
    private final StickCommand stickCommand = new StickCommand(boxStickItem);
    private final StickListener stickListener = new StickListener(boxStickItem);

    public StickFeature() {
        super("stick");
    }

    @Override
    public void enable() {
        BoxProvider.get().getBoxCommand().getSubCommandHolder().register(stickCommand);
        Bukkit.getPluginManager().registerEvents(stickListener, BoxProvider.get().getPluginInstance());
    }

    @Override
    public void disable() {
        BoxProvider.get().getBoxCommand().getSubCommandHolder().unregister(stickCommand);
        HandlerList.unregisterAll(stickListener);
    }
}
