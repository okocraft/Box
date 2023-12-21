package net.okocraft.box.feature.autostore;

import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.feature.autostore.command.AutoStoreCommand;
import net.okocraft.box.feature.autostore.gui.AutoStoreClickMode;
import net.okocraft.box.feature.autostore.listener.AutoSaveListener;
import net.okocraft.box.feature.autostore.listener.BoxPlayerListener;
import net.okocraft.box.feature.autostore.listener.ItemListener;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.AutoStoreSettingContainer;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AutoStoreFeature extends AbstractBoxFeature implements Disableable, Reloadable {

    public static final @NotNull Key AUTO_SAVE_LISTENER_KEY = Key.key("box", "feature/autostore/auto_save_listener");
    public static final @NotNull Key PLAYER_LISTENER_KEY = Key.key("box", "feature/autostore/player_listener");
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
        AutoStoreSettingContainer.INSTANCE.loadAll();

        this.boxPlayerListener.register(PLAYER_LISTENER_KEY);
        this.autoSaveListener.register(AUTO_SAVE_LISTENER_KEY);

        itemListener.register();

        BoxAPI.api().getBoxCommand().getSubCommandHolder().register(autoStoreCommand);
        ClickModeRegistry.register(autoStoreClickMode);
    }

    @Override
    public void disable() {
        BoxAPI.api().getBoxCommand().getSubCommandHolder().unregister(autoStoreCommand);
        ClickModeRegistry.unregister(autoStoreClickMode);

        itemListener.unregister();

        autoSaveListener.unregister(AUTO_SAVE_LISTENER_KEY);
        boxPlayerListener.unregister(PLAYER_LISTENER_KEY);

        AutoStoreSettingContainer.INSTANCE.unloadAll();
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        disable();
        enable();

        sender.sendMessage(AutoStoreMessage.RELOAD_SUCCESS);
    }
}
