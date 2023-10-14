package net.okocraft.box.core.scheduler;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.scheduler.BoxScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class FoliaSchedulerWrapper implements BoxScheduler {

    @Override
    public void runAsyncTask(@NotNull Runnable task) {
        Bukkit.getAsyncScheduler().runNow(BoxProvider.get().getPluginInstance(), ignored -> task.run());
    }

    @Override
    public void runEntityTask(@NotNull Entity entity, @NotNull Runnable task) {
        entity.getScheduler().run(BoxProvider.get().getPluginInstance(), ignored -> task.run(), null);
    }

    public void scheduleRepeatingAsyncTask(@NotNull Runnable task, @NotNull Duration interval, @NotNull BooleanSupplier condition) {
        Bukkit.getAsyncScheduler().runAtFixedRate(
                BoxProvider.get().getPluginInstance(),
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
