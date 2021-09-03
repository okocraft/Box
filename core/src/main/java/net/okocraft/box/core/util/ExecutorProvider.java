package net.okocraft.box.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ExecutorProvider {

    private static final String EXECUTOR_PREFIX = "box-";
    private static final Collection<ExecutorService> CREATED_EXECUTORS = new HashSet<>();
    private static final ScheduledExecutorService SCHEDULER = newSingleThreadScheduler("Scheduler");

    private static boolean IS_SHUTDOWN = false;

    public static @NotNull ScheduledExecutorService getScheduler() {
        checkShutdown();
        return SCHEDULER;
    }

    public static void shutdownAll() throws InterruptedException {
        IS_SHUTDOWN = true;

        for (var executor : CREATED_EXECUTORS) {
            if (!executor.isShutdown()) {
                if (executor instanceof ScheduledExecutorService) {
                    executor.shutdownNow();
                } else {
                    executor.shutdown();
                    //noinspection ResultOfMethodCallIgnored
                    executor.awaitTermination(30, TimeUnit.SECONDS);
                }
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

    private static void checkShutdown() {
        if (IS_SHUTDOWN) {
            throw new IllegalStateException("ExecutorProvider was shutdown");
        }
    }
}
