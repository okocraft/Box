package net.okocraft.box.autostore.listener;

import com.github.siroshun09.event4j.handlerlist.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.player.PlayerLoadEvent;
import net.okocraft.box.api.event.player.PlayerUnloadEvent;
import net.okocraft.box.autostore.message.AutoStoreMessage;
import net.okocraft.box.autostore.model.SettingManager;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class BoxPlayerListener {

    private final SettingManager settingManager;

    private Key listenerKey;

    public BoxPlayerListener(@NotNull SettingManager settingManager) {
        this.settingManager = settingManager;
    }

    public void register(@NotNull Key listenerKey) {
        this.listenerKey = listenerKey;

        var eventBus = BoxProvider.get().getEventBus();

        eventBus.getHandlerList(PlayerLoadEvent.class).subscribe(listenerKey, this::onLoad);

        eventBus.getHandlerList(PlayerUnloadEvent.class).subscribe(listenerKey, this::onUnload);
    }

    public void unregister() {
        if (listenerKey != null) {
            var eventBus = BoxProvider.get().getEventBus();
            eventBus.getHandlerList(PlayerLoadEvent.class).unsubscribeAll(listenerKey);
            eventBus.getHandlerList(PlayerUnloadEvent.class).unsubscribeAll(listenerKey);
        }
    }

    private void onLoad(@NotNull PlayerLoadEvent event) {
        var player = event.getBoxPlayer().getPlayer();

        settingManager.load(player)
                .exceptionallyAsync(throwable -> {
                    BoxProvider.get().getLogger().log(
                            Level.SEVERE,
                            "Could not load autostore setting (" + player.getName() + ")",
                            throwable
                    );

                    player.sendMessage(AutoStoreMessage.ERROR_FAILED_TO_LOAD_SETTINGS);

                    return null;
                });
    }

    private void onUnload(@NotNull PlayerUnloadEvent event) {
        var player = event.getBoxPlayer().getPlayer();

        settingManager.unload(player)
                .exceptionallyAsync(throwable -> {
                    BoxProvider.get().getLogger().log(
                            Level.SEVERE,
                            "Could not unload autostore setting (" + player.getName() + ")",
                            throwable
                    );

                    return null;
                });
    }
}
