package net.okocraft.box.core.util.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutorProvider {

    private static final String EXECUTOR_PREFIX = "Box ";

    private final Map<String, ExecutorService> registeredExecutors = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Logger logger;
    private final AtomicBoolean closed = new AtomicBoolean();

    public ExecutorProvider(@NotNull Logger logger) {
        this.logger = logger;
    }

    public @NotNull ExecutorService newSingleThreadExecutor(@NotNull String name) {
        checkClosed();

        if (registeredExecutors.containsKey(name)) {
            throw new IllegalArgumentException(name + " is already used.");
        }

        var executor = Executors.newSingleThreadExecutor(createThreadFactoryBuilder().setNameFormat(EXECUTOR_PREFIX + name).build());

        registeredExecutors.put(name, executor);

        return executor;
    }

    public @NotNull ScheduledExecutorService newSingleThreadScheduler(@NotNull String name) {
        checkClosed();

        if (registeredExecutors.containsKey(name)) {
            throw new IllegalArgumentException(name + " is already used.");
        }

        var scheduler = Executors.newSingleThreadScheduledExecutor(createThreadFactoryBuilder().setNameFormat(EXECUTOR_PREFIX + name).build());

        registeredExecutors.put(name, scheduler);

        return scheduler;
    }

    public void close() {
        if (closed.getAndSet(true)) {
            throw new IllegalStateException("ExecutorProvider is already closed.");
        }

        var executorNames = new ArrayList<>(registeredExecutors.keySet());

        Collections.reverse(executorNames);

        executorNames.forEach(this::shutdownExecutor);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void shutdownExecutor(@NotNull String name) {
        var executor = registeredExecutors.remove(name);

        if (executor == null) {
            throw new IllegalArgumentException(name + " is not registered.");
        }

        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("An exception occurred while shutting down executor.", e);
        }
    }

    private @NotNull ThreadFactoryBuilder createThreadFactoryBuilder() {
        return new ThreadFactoryBuilder().setDaemon(false).setUncaughtExceptionHandler(this::catchException);
    }

    private void catchException(@NotNull Thread thread, @NotNull Throwable exception) {
        logger.error("Uncaught exception at " + thread.getName(), exception);
    }

    private void checkClosed() {
        if (closed.get()) {
            throw new IllegalStateException("ExecutorProvider is already closed.");
        }
    }
}
