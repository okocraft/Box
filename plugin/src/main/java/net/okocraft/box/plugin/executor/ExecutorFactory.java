package net.okocraft.box.plugin.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

final class ExecutorFactory {

    private ExecutorFactory() {}

    @NotNull
    static ExecutorService createSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder().setNameFormat("Box-Storage-Thread").setDaemon(false).build()
        );
    }

    @NotNull
    static ExecutorService createForkJoinPool() {
        return new ForkJoinPool(15, ForkJoinPool.defaultForkJoinWorkerThreadFactory, (t, e) -> e.printStackTrace(), false);
    }

    @NotNull
    static ScheduledExecutorService createScheduledThreadPool() {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1,
                new ThreadFactoryBuilder().setNameFormat("Box-Scheduler").setDaemon(true).build()
        );

        scheduler.setRemoveOnCancelPolicy(true);

        return scheduler;
    }
}
