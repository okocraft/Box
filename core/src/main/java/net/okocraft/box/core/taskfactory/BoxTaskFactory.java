package net.okocraft.box.core.taskfactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.taskfactory.TaskFactory;
import net.okocraft.box.api.util.Folia;
import net.okocraft.box.api.util.MCDataVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public class BoxTaskFactory implements TaskFactory {

    private final ExecutorService executor = Executors.newFixedThreadPool(
            Math.min(Runtime.getRuntime().availableProcessors(), 4),
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("Box Worker - #%d")
                    .setUncaughtExceptionHandler(this::reportUncaughtException)
                    .build()
    );

    @Override
    public @NotNull CompletableFuture<Void> run(@NotNull Runnable task) {
        Objects.requireNonNull(task);
        return CompletableFuture.runAsync(task, getGlobalExecutor());
    }

    @Override
    public <E extends Entity> @NotNull CompletableFuture<Void> runEntityTask(@NotNull E target, @NotNull Consumer<E> task) {
        Objects.requireNonNull(task);
        return CompletableFuture.runAsync(() -> task.accept(target), getExecutorForEntity(target));
    }

    @Override
    public @NotNull <T> CompletableFuture<T> supply(@NotNull Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return CompletableFuture.supplyAsync(supplier, getGlobalExecutor());
    }

    @Override
    public <E extends Entity, T> @NotNull CompletableFuture<T> supplyFromEntity(@NotNull E entity, @NotNull Function<E, T> function) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(function);

        return CompletableFuture.supplyAsync(() -> function.apply(entity), getExecutorForEntity(entity));
    }

    @Override
    public @NotNull CompletableFuture<Void> runAsync(@NotNull Runnable task) {
        Objects.requireNonNull(task);
        return CompletableFuture.runAsync(task, executor);
    }

    @Override
    public @NotNull <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    public void shutdown() throws InterruptedException {
        executor.shutdown();

        //noinspection ResultOfMethodCallIgnored
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    private void reportUncaughtException(@NotNull Thread thread, @NotNull Throwable throwable) {
        BoxProvider.get().getLogger().log(
                Level.SEVERE,
                "An exception occurred in the thread " + thread.getName(),
                throwable
        );
    }

    private @NotNull Executor getGlobalExecutor() {
        return useModernExecutor() ?
                command -> Bukkit.getGlobalRegionScheduler().execute(BoxProvider.get().getPluginInstance(), command) :
                getMainThread();
    }

    private @NotNull Executor getExecutorForEntity(@NotNull Entity entity) {
        if (useModernExecutor()) {
            return command -> entity.getScheduler().run(BoxProvider.get().getPluginInstance(), $ -> command.run(), null);
        } else {
            return getMainThread();
        }
    }

    private @NotNull Executor getMainThread() {
        return Bukkit.getScheduler().getMainThreadExecutor(BoxProvider.get().getPluginInstance());
    }

    private boolean useModernExecutor() {
        // In Paper 1.20.1 Build 40, Folia's new scheduler APIs have been moved to Paper
        // Or, Box is running on Folia
        return MCDataVersion.CURRENT.isAfterOrSame(MCDataVersion.MC_1_20_1) || Folia.check();
    }
}
