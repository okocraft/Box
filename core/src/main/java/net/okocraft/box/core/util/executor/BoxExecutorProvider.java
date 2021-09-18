package net.okocraft.box.core.util.executor;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.util.ExecutorProvider;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BoxExecutorProvider implements ExecutorProvider {

    private final ForkJoinPool worker = new ForkJoinPool(
            Math.min(Runtime.getRuntime().availableProcessors(), 4),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            this::reportUncaughtException,
            false
    );

    @Override
    public @NotNull Executor getExecutor() {
        return worker;
    }

    @Override
    public @NotNull Executor getMainThread() {
        return Bukkit.getScheduler().getMainThreadExecutor(BoxProvider.get().getPluginInstance());
    }

    public void shutdown() throws InterruptedException {
        worker.shutdown();

        //noinspection ResultOfMethodCallIgnored
        worker.awaitTermination(1, TimeUnit.MINUTES);
    }


    private void reportUncaughtException(@NotNull Thread thread, @NotNull Throwable throwable) {
        BoxProvider.get().getLogger().log(
                Level.SEVERE,
                "An exception occurred in the thread " + thread.getName(),
                throwable
        );
    }
}
