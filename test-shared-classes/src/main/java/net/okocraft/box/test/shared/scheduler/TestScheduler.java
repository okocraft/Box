package net.okocraft.box.test.shared.scheduler;

import net.okocraft.box.api.scheduler.BoxScheduler;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public class TestScheduler implements BoxScheduler {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void runAsyncTask(@NotNull Runnable task) {
        this.scheduler.execute(task);
    }

    @Override
    public void runEntityTask(@NotNull Entity entity, @NotNull Runnable task) {
        this.scheduler.execute(task);
    }

    @Override
    public void scheduleAsyncTask(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        this.scheduler.schedule(task, delay, unit);
    }

    @Override
    public void scheduleRepeatingAsyncTask(@NotNull Runnable task, @NotNull Duration interval, @NotNull BooleanSupplier condition) {
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
