package net.okocraft.box.feature.autostore;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.feature.autostore.command.AutoStoreCommand;
import net.okocraft.box.feature.autostore.listener.BoxPlayerListener;
import net.okocraft.box.feature.autostore.listener.ItemListener;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.SettingManager;
import net.okocraft.box.feature.autostore.task.AutoStoreSettingSaveTask;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoStoreFeature extends AbstractBoxFeature implements Reloadable {

    private final SettingManager settingManager = new SettingManager();
    private final AutoStoreCommand autoStoreCommand = new AutoStoreCommand(settingManager);
    private final BoxPlayerListener boxPlayerListener = new BoxPlayerListener(settingManager);
    private final ItemListener itemListener = new ItemListener(settingManager);
    private final AutoStoreSettingSaveTask autoSaveTask = new AutoStoreSettingSaveTask(settingManager);

    private ScheduledExecutorService scheduler;

    public AutoStoreFeature() {
        super("autostore");
    }

    @Override
    public void enable() {
        settingManager.loadAll();
        BoxProvider.get().getBoxCommand().getSubCommandHolder().register(autoStoreCommand);
        boxPlayerListener.register(getListenerKey());
        itemListener.register();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(autoSaveTask, 10, 10, TimeUnit.MINUTES);
        autoSaveTask.registerListener(getListenerKey());
    }

    @Override
    public void disable() {
        itemListener.unregister();
        boxPlayerListener.unregister();
        BoxProvider.get().getBoxCommand().getSubCommandHolder().unregister(autoStoreCommand);

        if (scheduler != null) {
            scheduler.shutdownNow();
            autoSaveTask.unregisterListener(getListenerKey());
        }

        settingManager.unloadAll();
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        disable();
        enable();

        sender.sendMessage(AutoStoreMessage.RELOAD_SUCCESS);
    }

    public @NotNull SettingManager getSettingManager() {
        return settingManager;
    }
}
