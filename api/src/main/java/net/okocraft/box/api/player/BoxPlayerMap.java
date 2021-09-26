package net.okocraft.box.api.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * An interface to get the {@link BoxPlayer} from {@link Player}.
 */
public interface BoxPlayerMap {

    /**
     * Gets the {@link BoxPlayer}.
     *
     * @param player the {@link Player}
     * @return the {@link BoxPlayer}
     * @throws IllegalStateException if the player is not online or not loaded
     */
    @NotNull BoxPlayer get(@NotNull Player player) throws IllegalStateException;
}
