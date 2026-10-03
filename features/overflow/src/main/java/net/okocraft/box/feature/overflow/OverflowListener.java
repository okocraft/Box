package net.okocraft.box.feature.overflow;

import dev.siroshun.event4j.api.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.player.PlayerUnloadEvent;
import net.okocraft.box.api.event.stockholder.StockHolderSaveEvent;
import net.okocraft.box.api.event.stockholder.stock.StockOverflowEvent;
import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.SubscribedListenerHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

final class OverflowListener {

    private static final Key LISTENER_KEY = Key.key("box", "feature/overflow/listener");

    private final OverflowStockContainerRegistry containerRegistry;
    private final SubscribedListenerHolder listenerHolder = new SubscribedListenerHolder();

    OverflowListener(@NotNull OverflowStockContainerRegistry containerRegistry) {
        this.containerRegistry = containerRegistry;
    }

    void register() {
        this.listenerHolder.subscribeAll(subscriber ->
            subscriber.add(StockOverflowEvent.class, LISTENER_KEY, this::onOverflow, Priority.NORMAL)
                .add(StockHolderSaveEvent.class, LISTENER_KEY, this::onStockHolderSave, Priority.NORMAL)
                .add(PlayerUnloadEvent.class, LISTENER_KEY, this::onPlayerUnload, Priority.NORMAL)
        );
    }

    void unregister() {
        this.listenerHolder.unsubscribeAll();
    }

    private void onOverflow(@NotNull StockOverflowEvent event) {
        if (!(event.getStockHolder() instanceof PersonalStockHolder stockHolder)) {
            return;
        }

        UUID ownerUuid = stockHolder.getUser().getUUID();

        try {
            this.containerRegistry.increase(ownerUuid, event.getItem(), event.getExcess());
        } catch (Exception e) {
            BoxLogger.logger().error(
                "Could not store overflowed stock ({}, {})",
                ownerUuid,
                event.getItem().getPlainName(),
                e
            );
        }
    }

    private void onStockHolderSave(@NotNull StockHolderSaveEvent event) {
        if (!(event.getStockHolder() instanceof PersonalStockHolder stockHolder)) {
            return;
        }

        UUID ownerUuid = stockHolder.getUser().getUUID();

        try {
            this.containerRegistry.save(ownerUuid);
        } catch (Exception e) {
            BoxLogger.logger().error(
                "Could not save overflow stock for {}.",
                ownerUuid,
                e
            );
        }
    }

    private void onPlayerUnload(@NotNull PlayerUnloadEvent event) {
        UUID ownerUuid = event.getBoxPlayer().getUUID();

        try {
            this.containerRegistry.unload(ownerUuid);
        } catch (Exception e) {
            BoxLogger.logger().error(
                "Could not unload overflow stock for {}.",
                ownerUuid,
                e
            );
        }
    }
}
