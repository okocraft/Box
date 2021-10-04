package net.okocraft.box.api.taskfactory;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
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
     */
    @NotNull CompletableFuture<Void> run(@NotNull Runnable task);

    /**
     * Creates a {@link CompletableFuture} to supply values on main thread.
     *
     * @param supplier the supplier
     * @param <T>      the value type
     * @return the new {@link CompletableFuture}
     */
    <T> @NotNull CompletableFuture<T> supply(@NotNull Supplier<T> supplier);

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
