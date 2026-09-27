package net.okocraft.box.feature.overflow;

import dev.siroshun.event4j.api.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.stockholder.stock.StockOverflowEvent;
import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.SubscribedListenerHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

final class OverflowListener {

    private static final Key LISTENER_KEY = Key.key("box", "feature/overflow/stock_overflow_listener");

    private final OverflowStockHolderMap holderMap;
    private final OverflowStockHolderStore holderStore;
    private final SubscribedListenerHolder listenerHolder = new SubscribedListenerHolder();

    OverflowListener(
        @NotNull OverflowStockHolderMap holderMap,
        @NotNull OverflowStockHolderStore holderStore
    ) {
        this.holderMap = holderMap;
        this.holderStore = holderStore;
    }

    void register() {
        this.listenerHolder.subscribeAll(subscriber ->
            subscriber.add(StockOverflowEvent.class, LISTENER_KEY, this::onOverflow, Priority.NORMAL)
        );
    }

    void unregister() {
        this.listenerHolder.unsubscribeAll();
    }

    private void onOverflow(@NotNull StockOverflowEvent event) {
        StockHolder source = event.getStockHolder();

        if (!(source instanceof PersonalStockHolder)
            && !this.holderMap.isOverflowHolder(source.getUUID())) {
            return;
        }

        try {
            UUID overflowHolderUuid = this.holderMap.getOrCreateNext(source.getUUID());
            this.holderStore.increase(overflowHolderUuid, event.getItem(), event.getExcess());
        } catch (Exception e) {
            BoxLogger.logger().error(
                "Could not store overflowed stock ({}, {})",
                source.getUUID(),
                event.getItem().getPlainName(),
                e
            );
        }
    }
}
