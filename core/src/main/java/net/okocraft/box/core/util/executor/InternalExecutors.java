package net.okocraft.box.core.util.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class InternalExecutors {

    private static final String EXECUTOR_PREFIX = "Box ";

    private static final Collection<ExecutorService> CREATED_EXECUTORS = new HashSet<>();

    private static final ExecutorService EVENT_EXECUTOR = Executors.newFixedThreadPool(
            2,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Box Event Worker - #%d").build()
    );

    private static boolean IS_SHUTDOWN = false;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void shutdownAll() throws InterruptedException {
        IS_SHUTDOWN = true;

        EVENT_EXECUTOR.shutdown();
        EVENT_EXECUTOR.awaitTermination(30, TimeUnit.SECONDS);

        for (var executor : CREATED_EXECUTORS) {
            if (!executor.isShutdown()) {
                executor.shutdown();
                executor.awaitTermination(30, TimeUnit.SECONDS);
            }
        }

        CREATED_EXECUTORS.clear();
    }

    public static @NotNull ExecutorService newSingleThreadExecutor(@NotNull String name) {
        checkShutdown();

        var created = Executors.newSingleThreadExecutor(r -> new Thread(r, EXECUTOR_PREFIX + name));

        CREATED_EXECUTORS.add(created);

        return created;
    }

    public static @NotNull ScheduledExecutorService newSingleThreadScheduler(@NotNull String name) {
        checkShutdown();

        var created = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, EXECUTOR_PREFIX + name));

        CREATED_EXECUTORS.add(created);

        return created;
    }

    public static @NotNull Executor getEventExecutor() {
        return EVENT_EXECUTOR;
    }

    private static void checkShutdown() {
        if (IS_SHUTDOWN) {
            throw new IllegalStateException("ExecutorProvider was shutdown");
        }
    }
}
