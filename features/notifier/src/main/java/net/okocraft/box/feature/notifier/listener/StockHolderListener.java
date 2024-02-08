package net.okocraft.box.feature.notifier.listener;

import com.github.siroshun09.event4j.listener.ListenerBase;
import com.github.siroshun09.event4j.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockSetEvent;
import net.okocraft.box.api.model.manager.EventManager;
import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.feature.notifier.factory.NotificationFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class StockHolderListener {

    private static final Key STOCK_EVENT_LISTENER_KEY = Key.key("box", "feature/notifier/stock_event_listener");

    public static void register(@NotNull EventManager eventManager) {
        eventManager.subscribeAll(List.of(
                new ListenerBase<>(StockIncreaseEvent.class, STOCK_EVENT_LISTENER_KEY, StockHolderListener::onIncrease, Priority.NORMAL),
                new ListenerBase<>(StockDecreaseEvent.class, STOCK_EVENT_LISTENER_KEY, StockHolderListener::onDecrease, Priority.NORMAL),
                new ListenerBase<>(StockSetEvent.class, STOCK_EVENT_LISTENER_KEY, StockHolderListener::onSet, Priority.NORMAL)
        ));
    }

    public static void unregister(@NotNull EventManager eventManager) {
        eventManager.unsubscribeByKey(STOCK_EVENT_LISTENER_KEY);
    }

    private static void onIncrease(@NotNull StockIncreaseEvent event) {
        if (event.getStockHolder() instanceof PersonalStockHolder stockHolder) {
            NotificationFactory.create(event).increments(event.getIncrements()).showActionBar(stockHolder.getUser());
        }
    }

    private static void onDecrease(@NotNull StockDecreaseEvent event) {
        if (event.getStockHolder() instanceof PersonalStockHolder stockHolder) {
            NotificationFactory.create(event).decrements(event.getDecrements()).showActionBar(stockHolder.getUser());
        }
    }

    private static void onSet(@NotNull StockSetEvent event) {
        if (event.getStockHolder() instanceof PersonalStockHolder stockHolder) {
            NotificationFactory.create(event).previous(event.getPreviousAmount()).showActionBar(stockHolder.getUser());
        }
    }

    private StockHolderListener() {
        throw new UnsupportedOperationException();
    }
}
