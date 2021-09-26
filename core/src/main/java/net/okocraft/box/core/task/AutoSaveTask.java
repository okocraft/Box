package net.okocraft.box.core.task;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.general.AutoSaveStartEvent;
import net.okocraft.box.core.config.Settings;
import net.okocraft.box.core.util.executor.InternalExecutors;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AutoSaveTask {

    private final ScheduledExecutorService scheduler =
            InternalExecutors.newSingleThreadScheduler("auto-save-scheduler");

    private ScheduledFuture<?> task;

    public void start() {
        long interval = BoxProvider.get().getConfiguration().get(Settings.STOCK_DATA_SAVE_INTERVAL);
        task = scheduler.scheduleAtFixedRate(this::runTask, interval, interval, TimeUnit.SECONDS);
    }

    public void stop() {
        if (task != null) {
            task.cancel(true);
        }
    }

    private void runTask() {
        BoxProvider.get().getEventBus().callEvent(new AutoSaveStartEvent());
    }
}
