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
     * Creates a {@link CompletableFuture} to run the task on main thread.
     *
     * @param task the task to run
     * @return the new {@link CompletableFuture}
     * @deprecated not working on Folia
     */
    @Deprecated(since = "5.3.1")
    @NotNull CompletableFuture<Void> run(@NotNull Runnable task);

    <E extends Entity> @NotNull CompletableFuture<Void> runEntityTask(@NotNull E target, @NotNull Consumer<E> task);

    /**
     * Creates a {@link CompletableFuture} to supply values on main thread.
     *
     * @param supplier the supplier
     * @param <T>      the value type
     * @return the new {@link CompletableFuture}
     * @deprecated not working on Folia
     */
    @Deprecated(since = "5.3.1")
    <T> @NotNull CompletableFuture<T> supply(@NotNull Supplier<T> supplier);

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
