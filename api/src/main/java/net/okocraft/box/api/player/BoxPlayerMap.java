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
     * Checks if loading of data for the specified player is scheduled.
     *
     * @param player the {@link Player} to check
     * @return {@code true} indicates that it will be loaded a little later, {@code false} indicates that it has already been loaded or failed.
     */
    boolean isScheduledLoading(@NotNull Player player);

    /**
     * Gets the {@link BoxPlayer}.
     *
     * @param player the {@link Player}
     * @return the {@link BoxPlayer}
     * @throws IllegalStateException if the player is not online or not loaded
     */
    @NotNull BoxPlayer get(@NotNull Player player) throws IllegalStateException;
}
