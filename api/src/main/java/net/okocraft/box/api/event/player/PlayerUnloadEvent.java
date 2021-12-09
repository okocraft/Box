package net.okocraft.box.api.event.player;

import net.okocraft.box.api.player.BoxPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link PlayerEvent} called when the {@link BoxPlayer} is unloaded.
 */
public class PlayerUnloadEvent extends PlayerEvent{

    /**
     * The constructor of a {@link PlayerUnloadEvent}.
     *
     * @param boxPlayer the player of this event
     */
    public PlayerUnloadEvent(@NotNull BoxPlayer boxPlayer) {
        super(boxPlayer);
    }

    @Override
    public @NotNull String toDebugLog() {
        return "PlayerUnloadEvent{" +
                "uuid=" + getBoxPlayer().getUUID() +
                ", name=" + getBoxPlayer().getName() +
                '}';
    }

    @Override
    public String toString() {
        return "PlayerUnloadEvent{" +
                "boxPlayer=" + getBoxPlayer() +
                '}';
    }
}
