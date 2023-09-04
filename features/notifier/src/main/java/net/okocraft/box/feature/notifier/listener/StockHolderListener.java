package net.okocraft.box.feature.notifier.listener;

import com.github.siroshun09.event4j.key.Key;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockSetEvent;
import net.okocraft.box.feature.notifier.factory.NotificationFactory;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class StockHolderListener {

    private Key listenerKey;

    public void register(@NotNull Key listenerKey) {
        this.listenerKey = listenerKey;

        var eventBus = BoxProvider.get().getEventBus();

        eventBus.getSubscriber(StockIncreaseEvent.class).subscribe(listenerKey, this::onIncrease);
        eventBus.getSubscriber(StockDecreaseEvent.class).subscribe(listenerKey, this::onDecrease);
        eventBus.getSubscriber(StockSetEvent.class).subscribe(listenerKey, this::onSet);
    }

    public void unregister() {
        if (listenerKey == null) {
            return;
        }

        var eventBus = BoxProvider.get().getEventBus();

        eventBus.getSubscriber(StockIncreaseEvent.class).unsubscribeAll(listenerKey);
        eventBus.getSubscriber(StockDecreaseEvent.class).unsubscribeAll(listenerKey);
        eventBus.getSubscriber(StockSetEvent.class).unsubscribeAll(listenerKey);
    }

    public void onIncrease(@NotNull StockIncreaseEvent event) {
        var notification =
                NotificationFactory.create(event.getItem())
                        .current(event.getAmount())
                        .increments(event.getIncrements())
                        .build();

        notify(event, notification);
    }

    public void onDecrease(@NotNull StockDecreaseEvent event) {
        var notification =
                NotificationFactory.create(event.getItem())
                        .current(event.getAmount())
                        .decrements(event.getDecrements())
                        .build();

        notify(event, notification);
    }

    public void onSet(@NotNull StockSetEvent event) {
        var notification =
                NotificationFactory.create(event.getItem())
                        .current(event.getAmount())
                        .previous(event.getPreviousAmount())
                        .build();

        notify(event, notification);
    }

    private void notify(@NotNull StockEvent event, @NotNull Component notification) {
        var player = Bukkit.getPlayer(event.getStockHolder().getUUID());
        var playerMap = BoxProvider.get().getBoxPlayerMap();

        if (player == null || !playerMap.isLoaded(player)) {
            return;
        }

        var boxPlayer = playerMap.get(player);

        if (event.getStockHolder() == boxPlayer.getPersonalStockHolder()) {
            player.sendMessage(notification);
        }
    }
}
