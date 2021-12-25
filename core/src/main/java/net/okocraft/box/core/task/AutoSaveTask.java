package net.okocraft.box.core.task;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.general.AutoSaveStartEvent;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.core.config.Settings;
import net.okocraft.box.core.message.ErrorMessages;
import net.okocraft.box.core.model.queue.AutoSaveQueue;
import net.okocraft.box.core.util.executor.InternalExecutors;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AutoSaveTask {

    private final ScheduledExecutorService scheduler =
            InternalExecutors.newSingleThreadScheduler("auto-save-scheduler");

    private final AutoSaveQueue queue;

    public AutoSaveTask(@NotNull AutoSaveQueue queue) {
        this.queue = queue;
    }

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
        saveUserStockHolders();
    }

    private void saveUserStockHolders() {
        var loaders = queue.getQueue();
        queue.clear();

        for (var loader : loaders) {
            if (!loader.isLoaded()) {
                continue;
            }

            var userStockHolder = loader.getSource();

            if (Bukkit.getPlayer(loader.getUUID()) == null) {
                loader.unload();
            }

            try {
                BoxProvider.get().getStockManager().saveUserStock(userStockHolder).join();
            } catch (Exception e) {
                Optional.of(userStockHolder.getUser())
                        .map(BoxUser::getUUID)
                        .map(Bukkit::getPlayer)
                        .ifPresent(player -> player.sendMessage(ErrorMessages.ERROR_SAVE_PLAYER_DATA));

                BoxProvider.get().getLogger().log(
                        Level.SEVERE,
                        "Could not save the user's stockholder (" + userStockHolder.getUser().getUUID() + ")",
                        e
                );
            }
        }
    }
}
