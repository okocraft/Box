package net.okocraft.box.test.shared.scheduler;

import net.okocraft.box.api.scheduler.BoxScheduler;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class TestScheduler implements BoxScheduler {

    private final @Nullable ScheduledExecutorService scheduler;
    private final ConcurrentLinkedQueue<ScheduledTask> scheduledTasks = new ConcurrentLinkedQueue<>();

    public TestScheduler(boolean execute) {
        this.scheduler = execute ? Executors.newSingleThreadScheduledExecutor() : null;
    }

    @Override
    public void runAsyncTask(@NotNull Runnable task) {
        this.scheduledTasks.add(new ScheduledTask(Duration.ZERO, Duration.ZERO, ScheduledTask.Type.ASYNC));
        if (this.scheduler != null) this.scheduler.execute(task);

    }

    @Override
    public void runEntityTask(@NotNull Entity entity, @NotNull Runnable task) {
        this.scheduledTasks.add(new ScheduledTask(Duration.ZERO, Duration.ZERO, ScheduledTask.Type.ENTITY));
        if (this.scheduler != null) this.scheduler.execute(task);
    }

    @Override
    public void scheduleAsyncTask(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        this.scheduledTasks.add(new ScheduledTask(Duration.of(delay, unit.toChronoUnit()), Duration.ZERO, ScheduledTask.Type.ASYNC));
        if (this.scheduler != null) this.scheduler.schedule(task, delay, unit);
    }

    @Override
    public void scheduleRepeatingAsyncTask(@NotNull Runnable task, @NotNull Duration interval, @NotNull BooleanSupplier condition) {
        this.scheduledTasks.add(new ScheduledTask(interval, interval, ScheduledTask.Type.ASYNC));
        if (this.scheduler != null) {
            var ref = new AtomicReference<ScheduledFuture<?>>();

            ref.set(this.scheduler.scheduleWithFixedDelay(() -> {
                if (condition.getAsBoolean()) {
                    task.run();
                } else {
                    ref.get().cancel(false);
                }
            }, interval.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS));
        }
    }

    public void checkTask(@NotNull Consumer<ScheduledTask> checker) {
        var task = this.scheduledTasks.poll();
        Assertions.assertNotNull(task);
        checker.accept(task);
    }
}
