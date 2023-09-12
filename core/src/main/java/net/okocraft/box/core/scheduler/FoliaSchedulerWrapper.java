package net.okocraft.box.core.scheduler;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.scheduler.BoxScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class FoliaSchedulerWrapper implements BoxScheduler {

    @Override
    public void runAsyncTask(@NotNull Runnable task) {
        Bukkit.getAsyncScheduler().runNow(BoxProvider.get().getPluginInstance(), ignored -> task.run());
    }

    @Override
    public void runEntityTask(@NotNull Entity entity, @NotNull Runnable task) {
        entity.getScheduler().run(BoxProvider.get().getPluginInstance(), ignored -> task.run(), null);
    }

}
