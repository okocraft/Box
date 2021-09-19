package net.okocraft.box.core.util.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.util.ExecutorProvider;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BoxExecutorProvider implements ExecutorProvider {

    private final ExecutorService executor = Executors.newFixedThreadPool(
            Math.min(Runtime.getRuntime().availableProcessors(), 4),
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("box-worker-%d")
                    .setUncaughtExceptionHandler(this::reportUncaughtException)
                    .build()
    );

    @Override
    public @NotNull Executor getExecutor() {
        return executor;
    }

    @Override
    public @NotNull Executor getMainThread() {
        return Bukkit.getScheduler().getMainThreadExecutor(BoxProvider.get().getPluginInstance());
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
}
