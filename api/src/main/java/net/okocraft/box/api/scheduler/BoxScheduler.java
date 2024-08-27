package net.okocraft.box.api.scheduler;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

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
     * @param task   a {@link Runnable} to run
     */
    void runEntityTask(@NotNull Entity entity, @NotNull Runnable task);

    /**
     * Schedules a delayed task.
     *
     * @param task  a {@link Runnable} to run
     * @param delay a delay
     * @param unit  a {@link TimeUnit} of delay
     */
    void scheduleAsyncTask(@NotNull Runnable task, long delay, @NotNull TimeUnit unit);

    /**
     * Schedules a repeating task.
     *
     * @param task      a {@link Runnable} to run
     * @param interval  an interval
     * @param condition a {@link BooleanSupplier} that decides the task should be continued
     */
    void scheduleRepeatingAsyncTask(@NotNull Runnable task, @NotNull Duration interval, @NotNull BooleanSupplier condition);

}
