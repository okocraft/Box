package net.okocraft.box.api.event;

/**
 * An interface that indicates the cancel status of the event.
 */
public interface Cancellable {

    /**
     * Gets if the event has been cancelled.
     *
     * @return {@code true} if canceled, {@code false} otherwise.
     */
    boolean isCancelled();

    /**
     * Sets the cancel status of the event.
     *
     * @param cancel {@code true} to cancel, {@code false} to not.
     */
    void setCancelled(boolean cancel);
}
