package net.okocraft.box.feature.stick;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.feature.stick.command.CustomStickCommand;
import net.okocraft.box.feature.stick.command.StickCommand;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import net.okocraft.box.feature.stick.listener.StickListener;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StickFeature extends AbstractBoxFeature implements Disableable {

    private final NamespacedKey key = BoxProvider.get().createNamespacedKey("stick");

    private final BoxStickItem boxStickItem = new BoxStickItem(key);
    private final StickCommand stickCommand = new StickCommand(boxStickItem);
    private final CustomStickCommand customStickCommand = new CustomStickCommand(key);
    private final StickListener stickListener = new StickListener(boxStickItem);

    public StickFeature() {
        super("stick");
    }

    @Override
    public void enable() {
        BoxProvider.get().getBoxCommand().getSubCommandHolder().register(stickCommand);
        BoxProvider.get().getBoxAdminCommand().getSubCommandHolder().register(customStickCommand);
        Bukkit.getPluginManager().registerEvents(stickListener, BoxProvider.get().getPluginInstance());
    }

    @Override
    public void disable() {
        BoxProvider.get().getBoxCommand().getSubCommandHolder().unregister(stickCommand);
        BoxProvider.get().getBoxAdminCommand().getSubCommandHolder().register(customStickCommand);
        HandlerList.unregisterAll(stickListener);
    }

    public @NotNull BoxStickItem getBoxStickItem() {
        return boxStickItem;
    }
}
