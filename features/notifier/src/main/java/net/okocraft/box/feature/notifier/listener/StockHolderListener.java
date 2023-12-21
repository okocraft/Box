package net.okocraft.box.feature.notifier.listener;

import com.github.siroshun09.event4j.listener.ListenerBase;
import com.github.siroshun09.event4j.priority.Priority;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockSetEvent;
import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.feature.notifier.factory.NotificationFactory;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class StockHolderListener {

    public void register(@NotNull Key listenerKey) {
        BoxAPI.api().getEventManager().subscribeAll(List.of(
                new ListenerBase<>(StockIncreaseEvent.class, listenerKey, this::onIncrease, Priority.NORMAL),
                new ListenerBase<>(StockDecreaseEvent.class, listenerKey, this::onDecrease, Priority.NORMAL),
                new ListenerBase<>(StockSetEvent.class, listenerKey, this::onSet, Priority.NORMAL)
        ));
    }

    public void unregister(@NotNull Key listenerKey) {
        BoxAPI.api().getEventManager().unsubscribeByKey(listenerKey);
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
        if (event.getStockHolder() instanceof PersonalStockHolder) {
            Optional.of(event.getStockHolder())
                    .map(StockHolder::getUUID)
                    .map(Bukkit::getPlayer)
                    .ifPresent(player -> player.sendActionBar(notification));
        }
    }
}
