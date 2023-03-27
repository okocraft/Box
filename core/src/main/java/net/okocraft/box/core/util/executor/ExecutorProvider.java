package net.okocraft.box.core.util.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutorProvider {

    private static final String EXECUTOR_PREFIX = "Box ";

    private final Map<String, ExecutorService> registeredExecutors = new ConcurrentHashMap<>();
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void shutdownExecutor(@NotNull String name) {
        checkClosed();

        var executor = registeredExecutors.remove(name);

        if (executor == null) {
            throw new IllegalArgumentException(name + " is not registered or already shutdown.");
        }

        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("An exception occurred while shutting down executor.", e);
        }
    }

    public void close() {
        checkClosed();
        closed.set(true);
        Set.copyOf(registeredExecutors.keySet()).forEach(this::shutdownExecutor);
    }

    private @NotNull ThreadFactoryBuilder createThreadFactoryBuilder() {
        return new ThreadFactoryBuilder().setDaemon(false).setUncaughtExceptionHandler(this::catchException);
    }

    private void catchException(@NotNull Thread thread, @NotNull Throwable exception) {
        logger.error("Uncaught exception at " + thread.getName(), exception);
    }

    private void checkClosed() {
        if (closed.get()) {
            throw new IllegalStateException("InternalExecutors was shutdown");
        }
    }
}
