package net.okocraft.box.compatible.paper;

import net.okocraft.box.api.scheduler.BoxScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class FoliaSchedulerWrapper implements BoxScheduler {

    private final Plugin plugin;

    public FoliaSchedulerWrapper(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runAsyncTask(@NotNull Runnable task) {
        Bukkit.getAsyncScheduler().runNow(this.plugin, ignored -> task.run());
    }

    @Override
    public void runEntityTask(@NotNull Entity entity, @NotNull Runnable task) {
        entity.getScheduler().run(this.plugin, ignored -> task.run(), null);
    }

    @Override
    public void scheduleAsyncTask(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        Bukkit.getAsyncScheduler().runDelayed(this.plugin, ignored -> task.run(), delay, unit);
    }

    @Override
    public void scheduleRepeatingAsyncTask(@NotNull Runnable task, @NotNull Duration interval, @NotNull BooleanSupplier condition) {
        Bukkit.getAsyncScheduler().runAtFixedRate(
                this.plugin,
                scheduledTask -> {
                    if (condition.getAsBoolean()) {
                        task.run();
                    } else {
                        scheduledTask.cancel();
                    }
                },
                interval.toMillis(),
                interval.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }
}
