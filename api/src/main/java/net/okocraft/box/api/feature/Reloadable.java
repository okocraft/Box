package net.okocraft.box.api.feature;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to indicate that it is reloadable.
 */
public interface Reloadable {

    /**
     * Reloads this.
     *
     * @param sender the sender who executed reload
     */
    void reload(@NotNull CommandSender sender);

}
