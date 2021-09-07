package net.okocraft.box.api.event.player;

import com.github.siroshun09.event4j.event.Event;
import net.okocraft.box.api.player.BoxPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A class that represents a {@link BoxPlayer} related event.
 */
public class PlayerEvent extends Event {

    private final BoxPlayer boxPlayer;

    /**
     * The constructor of a {@link PlayerEvent}.
     *
     * @param boxPlayer the player of this event
     */
    public PlayerEvent(@NotNull BoxPlayer boxPlayer) {
        this.boxPlayer = boxPlayer;
    }

    /**
     * Gets the player of this event.
     *
     * @return the player of this event
     */
    public @NotNull BoxPlayer getBoxPlayer() {
        return boxPlayer;
    }
}
