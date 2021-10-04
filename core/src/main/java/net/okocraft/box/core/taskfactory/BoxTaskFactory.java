package net.okocraft.box.core.taskfactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.taskfactory.TaskFactory;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;

public class BoxTaskFactory implements TaskFactory {

    private final ExecutorService executor = Executors.newFixedThreadPool(
            Math.min(Runtime.getRuntime().availableProcessors(), 4),
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("box-worker-%d")
                    .setUncaughtExceptionHandler(this::reportUncaughtException)
                    .build()
    );

    @Override
    public @NotNull CompletableFuture<Void> run(@NotNull Runnable task) {
        return CompletableFuture.runAsync(task, getMainThread());
    }

    @Override
    public @NotNull <T> CompletableFuture<T> supply(@NotNull Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, getMainThread());
    }

    @Override
    public @NotNull CompletableFuture<Void> runAsync(@NotNull Runnable task) {
        return CompletableFuture.runAsync(task, executor);
    }

    @Override
    public @NotNull <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> supplier) {
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

    public @NotNull Executor getExecutor() {
        return executor;
    }

    public @NotNull Executor getMainThread() {
        return Bukkit.getScheduler().getMainThreadExecutor(BoxProvider.get().getPluginInstance());
    }
}
