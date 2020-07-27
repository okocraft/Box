package net.okocraft.box.plugin.listener.stick;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.listener.AbstractListener;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractStickListener extends AbstractListener {

    public AbstractStickListener(@NotNull Box plugin) {
        super(plugin);
    }

    protected boolean isInDisabledWorld(@NotNull Player player) {
        return plugin.getGeneralConfig().getDisabledWorlds().contains(player.getWorld().getName());
    }

    protected boolean hasStick(@NotNull Player player, boolean inMain) {
        if (inMain) {
            return plugin.getItemManager().getStick().isStick(player.getInventory().getItemInMainHand());
        } else {
            return plugin.getItemManager().getStick().isStick(player.getInventory().getItemInOffHand());
        }
    }

    protected boolean isCreative(@NotNull Player player) {
        return player.getGameMode() == GameMode.CREATIVE;
    }
}
