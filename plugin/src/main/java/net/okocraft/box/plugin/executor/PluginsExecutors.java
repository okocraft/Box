package net.okocraft.box.plugin.executor;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.util.UnsafeRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PluginsExecutors {

    private final Box plugin;
    private final Set<ExecutorService> executors;

    private final ExecutorService worker;
    private final ExecutorService storageThread;
    private final ScheduledExecutorService scheduler;

    public PluginsExecutors(@NotNull Box plugin) {
        this.plugin = plugin;

        storageThread = ExecutorFactory.createSingleThreadExecutor();
        worker = ExecutorFactory.createForkJoinPool();
        scheduler = ExecutorFactory.createScheduledThreadPool();

        this.executors = new HashSet<>(Set.of(worker, storageThread, scheduler));
    }

    @NotNull
    public BukkitTask runSync(@NotNull Runnable runnable) {
        return plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    @NotNull
    public BukkitTask runSync(@NotNull UnsafeRunnable unsafeRunnable) {
        return runSync(unsafeRunnable.toRunnable());
    }

    @NotNull
    public ExecutorService getWorker() {
        return worker;
    }

    @NotNull
    public ExecutorService getStorageThread() {
        return storageThread;
    }

    @NotNull
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public void shutdown() {
        for (ExecutorService executor : executors) {
            if (!executor.isShutdown()) {
                executor.shutdown();

                try {
                    executor.awaitTermination(1, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
