package net.okocraft.box.api.event;

import org.jetbrains.annotations.ApiStatus;

/**
 * An interface representing an event that is called in a different thread than the thread that processes the Box.
 * @deprecated will be removed in v6
 */
@Deprecated(since = "5.5.2", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
public interface AsyncEvent {
}
