package net.okocraft.box.api.taskfactory;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An interface to create {@link CompletableFuture}s.
 */
public interface TaskFactory {

    /**
     * Creates a {@link CompletableFuture} to run the task on the main thread.
     * <p>
     * In Folia, the {@link Runnable} will be executed on {@link org.bukkit.Bukkit#getGlobalRegionScheduler()}.
     *
     * @param task the task to run
     * @return the new {@link CompletableFuture}
     */
    @NotNull CompletableFuture<Void> run(@NotNull Runnable task);

    /**
     * Creates a {@link CompletableFuture} to run the task on the main thread or entity's thread.
     * <p>
     * This task will be executed on the next tick.
     *
     * @param target the entity for which a task is performed
     * @param task   the task
     * @param <E>    the entity type
     * @return the {@link CompletableFuture}
     * @since 5.3.1
     */
    <E extends Entity> @NotNull CompletableFuture<Void> runEntityTask(@NotNull E target, @NotNull Consumer<E> task);

    /**
     * Creates a {@link CompletableFuture} to supply values on the main thread.
     * <p>
     * In Folia, the {@link Supplier} will be executed on {@link org.bukkit.Bukkit#getGlobalRegionScheduler()}.
     *
     * @param supplier the supplier
     * @param <T>      the value type
     * @return the new {@link CompletableFuture}
     */
    <T> @NotNull CompletableFuture<T> supply(@NotNull Supplier<T> supplier);

    /**
     * Creates a {@link CompletableFuture} to supply values on the main thread or entity's thread.
     *
     * @param entity   the entity to get value from
     * @param function the {@link Function} to get the value from the entity
     * @param <E>      the entity type
     * @param <T>      the value type
     * @return the new {@link CompletableFuture}
     * @since 5.3.1
     */
    <E extends Entity, T> @NotNull CompletableFuture<T> supplyFromEntity(@NotNull E entity, @NotNull Function<E, T> function);

    /**
     * Creates a {@link CompletableFuture} to run the task asynchronously.
     *
     * @param task the task to run
     * @return the new {@link CompletableFuture}
     */
    @NotNull CompletableFuture<Void> runAsync(@NotNull Runnable task);

    /**
     * Creates a {@link CompletableFuture} to supply values asynchronously.
     *
     * @param supplier the supplier
     * @param <T>      the value type
     * @return the new {@link CompletableFuture}
     */
    <T> @NotNull CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier);

}
