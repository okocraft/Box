package net.okocraft.box.api.event.player;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.player.BoxPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class that represents a {@link BoxPlayer} related event.
 */
public class PlayerEvent extends BoxEvent {

    private final BoxPlayer boxPlayer;

    /**
     * The constructor of a {@link PlayerEvent}.
     *
     * @param boxPlayer the player of this event
     */
    public PlayerEvent(@NotNull BoxPlayer boxPlayer) {
        this.boxPlayer = Objects.requireNonNull(boxPlayer);
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
