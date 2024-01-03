package net.okocraft.box.api.event.general;

import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * An event called when the auto save task started.
 *
 * @deprecated This event will be no longer called in Box v6
 */
@Deprecated(forRemoval = true, since = "5.5.2")
@ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
public class AutoSaveStartEvent extends BoxEvent {

    @Override
    public @NotNull String toDebugLog() {
        return getEventName();
    }

}
