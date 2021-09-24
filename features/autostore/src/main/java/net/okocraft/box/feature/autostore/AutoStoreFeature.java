package net.okocraft.box.feature.autostore;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.feature.autostore.command.AutoStoreCommand;
import net.okocraft.box.feature.autostore.gui.AutoStoreClickMode;
import net.okocraft.box.feature.autostore.listener.BoxPlayerListener;
import net.okocraft.box.feature.autostore.listener.ItemListener;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.container.AutoStoreSettingContainer;
import net.okocraft.box.feature.autostore.task.AutoStoreSettingSaveTask;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoStoreFeature extends AbstractBoxFeature implements Reloadable {

    private static final AutoStoreSettingContainer CONTAINER = new AutoStoreSettingContainer();

    public static @NotNull AutoStoreSettingContainer container() {
        return CONTAINER;
    }

    private final BoxPlayerListener boxPlayerListener = new BoxPlayerListener();
    private final ItemListener itemListener = new ItemListener();
    private final AutoStoreSettingSaveTask autoSaveTask = new AutoStoreSettingSaveTask();

    private final AutoStoreCommand autoStoreCommand = new AutoStoreCommand();
    private final AutoStoreClickMode autoStoreClickMode = new AutoStoreClickMode();

    private ScheduledExecutorService scheduler;

    public AutoStoreFeature() {
        super("autostore");
    }

    @Override
    public void enable() {
        CONTAINER.loadAll();
        boxPlayerListener.register(getListenerKey());
        itemListener.register();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(autoSaveTask, 10, 10, TimeUnit.MINUTES);
        autoSaveTask.registerListener(getListenerKey());

        BoxProvider.get().getBoxCommand().getSubCommandHolder().register(autoStoreCommand);
        ClickModeRegistry.register(autoStoreClickMode);
    }

    @Override
    public void disable() {
        BoxProvider.get().getBoxCommand().getSubCommandHolder().unregister(autoStoreCommand);
        ClickModeRegistry.unregister(autoStoreClickMode);

        itemListener.unregister();
        boxPlayerListener.unregister();

        if (scheduler != null) {
            scheduler.shutdownNow();
            autoSaveTask.unregisterListener(getListenerKey());
        }

        CONTAINER.unloadAll();
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        disable();
        enable();

        sender.sendMessage(AutoStoreMessage.RELOAD_SUCCESS);
    }
}
