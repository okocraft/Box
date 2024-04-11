package net.okocraft.box.api.event;

import org.jetbrains.annotations.NotNull;

/**
 * A superclass for all events fired by the Box.
 */
public class BoxEvent {

    /**
     * The constructor of {@link BoxEvent}.
     */
    public BoxEvent() {
    }

    /**
     * Creates the debug log from this event.
     *
     * @return the debug log
     */
    public @NotNull String toDebugLog() {
        return toString();
    }

}
