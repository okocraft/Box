package net.okocraft.box.autostore;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.autostore.command.AutoStoreCommand;
import net.okocraft.box.autostore.listener.BoxPlayerListener;
import net.okocraft.box.autostore.listener.ItemListener;
import net.okocraft.box.autostore.message.AutoStoreMessage;
import net.okocraft.box.autostore.model.SettingManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AutoStoreFeature extends AbstractBoxFeature implements Reloadable {

    private final SettingManager settingManager = new SettingManager();
    private final AutoStoreCommand autoStoreCommand = new AutoStoreCommand(settingManager);
    private final BoxPlayerListener boxPlayerListener = new BoxPlayerListener(settingManager);
    private final ItemListener itemListener = new ItemListener(settingManager);

    public AutoStoreFeature() {
        super("autostore");
    }

    @Override
    public void enable() {
        settingManager.loadAll();
        BoxProvider.get().getBoxCommand().getSubCommandHolder().register(autoStoreCommand);
        boxPlayerListener.register(getListenerKey());
        itemListener.register();
    }

    @Override
    public void disable() {
        itemListener.unregister();
        boxPlayerListener.unregister();
        BoxProvider.get().getBoxCommand().getSubCommandHolder().unregister(autoStoreCommand);

        settingManager.unloadAll();
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        disable();
        enable();

        sender.sendMessage(AutoStoreMessage.RELOAD_SUCCESS);
    }
}
