package net.okocraft.box.api.scheduler;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * An interface to schedule tasks.
 * <p>
 * This interface does not have some scheduling methods like delaying or repeating.
 */
public interface BoxScheduler {

    /**
     * Executes a task on the async scheduler.
     *
     * @param task a {@link Runnable} to run
     */
    void runAsyncTask(@NotNull Runnable task);

    /**
     * Executes a task on the entity scheduler.
     *
     * @param entity a {@link Entity} to get its scheduler
     * @param task a {@link Runnable} to run
     */
    void runEntityTask(@NotNull Entity entity, @NotNull Runnable task);

}
