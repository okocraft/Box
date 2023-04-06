package net.okocraft.box.core.taskfactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.taskfactory.TaskFactory;
import net.okocraft.box.api.util.Folia;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

        if (Folia.check()) {
            throw new UnsupportedOperationException("This method is not supported on Folia.");
        }

        return CompletableFuture.runAsync(task, getMainThread());
    }

    @Override
    public @NotNull CompletableFuture<Void> runTaskForPlayer(@NotNull Player target, @NotNull Consumer<Player> task) {
        Objects.requireNonNull(task);
        if (Folia.check()) {
            return CompletableFuture.runAsync(() -> task.accept(target), createExecutorFromEntityScheduler(target.getScheduler()));
        } else {
            return CompletableFuture.runAsync(() -> task.accept(target), getMainThread());
        }
    }

    @Override
    public @NotNull <T> CompletableFuture<T> supply(@NotNull Supplier<T> supplier) {
        Objects.requireNonNull(supplier);

        if (Folia.check()) {
            throw new UnsupportedOperationException("This method is not supported on Folia.");
        }

        return CompletableFuture.supplyAsync(supplier, getMainThread());
    }

    @Override
    public @NotNull <T> CompletableFuture<T> supplyFromPlayer(@NotNull Player player, @NotNull Function<Player, T> function) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(function);

        if (Folia.check()) {
            return CompletableFuture.supplyAsync(() -> function.apply(player), createExecutorFromEntityScheduler(player.getScheduler()));
        } else {
            return CompletableFuture.supplyAsync(() -> function.apply(player), getMainThread());
        }
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

    public @NotNull Executor getExecutor() {
        return executor;
    }

    public @NotNull Executor getMainThread() {
        return Bukkit.getScheduler().getMainThreadExecutor(BoxProvider.get().getPluginInstance());
    }

    private @NotNull Executor createExecutorFromEntityScheduler(@NotNull EntityScheduler scheduler) {
        return command -> scheduler.run(BoxProvider.get().getPluginInstance(), $ -> command.run(), null);
    }
}
