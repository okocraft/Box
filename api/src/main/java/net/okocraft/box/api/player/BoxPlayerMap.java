package net.okocraft.box.api.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * An interface to get the {@link BoxPlayer} from {@link Player}.
 */
public interface BoxPlayerMap {

    /**
     * Checks if the {@link BoxPlayer} of specified player is loaded.
     *
     * @param player the {@link Player} to check
     * @return {@code true} if loaded, {@code false} otherwise
     */
    boolean isLoaded(@NotNull Player player);

    /**
     * Gets the {@link BoxPlayer}.
     *
     * @param player the {@link Player}
     * @return the {@link BoxPlayer}
     * @throws IllegalStateException if the player is not online or not loaded
     */
    @NotNull BoxPlayer get(@NotNull Player player) throws IllegalStateException;
}
