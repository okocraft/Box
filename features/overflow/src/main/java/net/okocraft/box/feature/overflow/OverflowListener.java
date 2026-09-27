package net.okocraft.box.feature.overflow;

import dev.siroshun.event4j.api.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.stockholder.stock.StockOverflowEvent;
import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.SubscribedListenerHolder;
import org.jetbrains.annotations.NotNull;

final class OverflowListener {

    private static final Key LISTENER_KEY = Key.key("box", "feature/overflow/stock_overflow_listener");

    private final OverflowManager manager;
    private final SubscribedListenerHolder listenerHolder = new SubscribedListenerHolder();

    OverflowListener(@NotNull OverflowManager manager) {
        this.manager = manager;
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
        if (!(event.getStockHolder() instanceof PersonalStockHolder stockHolder)) {
            return;
        }

        try {
            this.manager.store(stockHolder.getUUID(), event.getItem(), event.getExcess());
        } catch (Exception e) {
            BoxLogger.logger().error(
                "Could not store overflowed stock ({}, {})",
                stockHolder.getUUID(),
                event.getItem().getPlainName(),
                e
            );
        }
    }
}
