package net.okocraft.box.api.event.general;

import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.NotNull;

/**
 * An event called when the auto save task started.
 */
public class AutoSaveStartEvent extends BoxEvent {

    @Override
    public @NotNull String toDebugLog() {
        return this.getClass().getSimpleName();
    }

}
