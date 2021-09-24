package net.okocraft.box.feature.autostore;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.feature.autostore.command.AutoStoreCommand;
import net.okocraft.box.feature.autostore.gui.AutoStoreClickMode;
import net.okocraft.box.feature.autostore.listener.AutoSaveListener;
import net.okocraft.box.feature.autostore.listener.BoxPlayerListener;
import net.okocraft.box.feature.autostore.listener.ItemListener;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.container.AutoStoreSettingContainer;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AutoStoreFeature extends AbstractBoxFeature implements Reloadable {

    private static final AutoStoreSettingContainer CONTAINER = new AutoStoreSettingContainer();

    public static @NotNull AutoStoreSettingContainer container() {
        return CONTAINER;
    }

    private final BoxPlayerListener boxPlayerListener = new BoxPlayerListener();
    private final AutoSaveListener autoSaveListener = new AutoSaveListener();
    private final ItemListener itemListener = new ItemListener();

    private final AutoStoreCommand autoStoreCommand = new AutoStoreCommand();
    private final AutoStoreClickMode autoStoreClickMode = new AutoStoreClickMode();

    public AutoStoreFeature() {
        super("autostore");
    }

    @Override
    public void enable() {
        CONTAINER.loadAll();

        boxPlayerListener.register(getListenerKey());
        autoSaveListener.register(getListenerKey());

        itemListener.register();

        BoxProvider.get().getBoxCommand().getSubCommandHolder().register(autoStoreCommand);
        ClickModeRegistry.register(autoStoreClickMode);
    }

    @Override
    public void disable() {
        BoxProvider.get().getBoxCommand().getSubCommandHolder().unregister(autoStoreCommand);
        ClickModeRegistry.unregister(autoStoreClickMode);

        itemListener.unregister();

        autoSaveListener.unregister();
        boxPlayerListener.unregister();

        CONTAINER.unloadAll();
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        disable();
        enable();

        sender.sendMessage(AutoStoreMessage.RELOAD_SUCCESS);
    }
}
