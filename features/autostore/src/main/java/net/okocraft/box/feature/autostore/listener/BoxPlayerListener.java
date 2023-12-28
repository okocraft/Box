package net.okocraft.box.feature.autostore.listener;

import com.github.siroshun09.event4j.listener.ListenerBase;
import com.github.siroshun09.event4j.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.player.PlayerLoadEvent;
import net.okocraft.box.api.event.player.PlayerUnloadEvent;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.autostore.model.AutoStoreSettingContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BoxPlayerListener {

    private final AutoStoreSettingContainer container;

    public BoxPlayerListener(@NotNull AutoStoreSettingContainer container) {
        this.container = container;
    }

    public void register(@NotNull Key listenerKey) {
        BoxAPI.api().getEventManager().subscribeAll(List.of(
                new ListenerBase<>(PlayerLoadEvent.class, listenerKey, this::onLoad, Priority.NORMAL),
                new ListenerBase<>(PlayerUnloadEvent.class, listenerKey, this::onUnload, Priority.NORMAL)
        ));
    }

    public void unregister(@NotNull Key listenerKey) {
        BoxAPI.api().getEventManager().unsubscribeByKey(listenerKey);
    }

    private void onLoad(@NotNull PlayerLoadEvent event) {
        var player = event.getBoxPlayer().getPlayer();

        try {
            this.container.load(player);
        } catch (Exception e) {
            BoxLogger.logger().error("Could not load autostore setting ({})", player.getName(), e);
            this.container.getLoadErrorMessage().source(BoxAPI.api().getMessageProvider().findSource(player)).send(player);
        }
    }

    private void onUnload(@NotNull PlayerUnloadEvent event) {
        var player = event.getBoxPlayer().getPlayer();

        try {
            this.container.unload(player);
        } catch (Exception e) {
            BoxLogger.logger().error("Could not unload autostore setting ({})", player.getName(), e);
        }
    }
}
