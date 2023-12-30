package net.okocraft.box.api.event.player;

import net.okocraft.box.api.player.BoxPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link PlayerEvent} called when the {@link BoxPlayer} is loaded.
 */
public class PlayerLoadEvent extends PlayerEvent {

    /**
     * The constructor of a {@link PlayerLoadEvent}.
     *
     * @param boxPlayer the loaded player
     */
    public PlayerLoadEvent(@NotNull BoxPlayer boxPlayer) {
        super(boxPlayer);
    }

}
