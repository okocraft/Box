package net.okocraft.feature.notifier.listener;

import com.github.siroshun09.event4j.handlerlist.Key;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.player.PlayerStockHolderChangeEvent;
import net.okocraft.box.api.event.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stock.StockEvent;
import net.okocraft.box.api.event.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stock.StockSetEvent;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.feature.notifier.factory.NotificationFactory;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class StockHolderListener {

    // stockholder uuid - player uuids
    private final Map<UUID, List<UUID>> stockHolderMap = new HashMap<>();

    private Key listenerKey;

    public void register(@NotNull Key listenerKey) {
        for (var player : Bukkit.getOnlinePlayers()) {
            var boxPlayer = BoxProvider.get().getBoxPlayerMap().get(player);

            var stockHolder = boxPlayer.getCurrentStockHolder();

            if (!(stockHolder instanceof UserStockHolder)) {
                addToMap(stockHolder.getUUID(), player.getUniqueId());
            }
        }

        this.listenerKey = listenerKey;

        var eventBus = BoxProvider.get().getEventBus();

        eventBus.getHandlerList(StockIncreaseEvent.class).subscribe(listenerKey, this::onIncrease);
        eventBus.getHandlerList(StockDecreaseEvent.class).subscribe(listenerKey, this::onDecrease);
        eventBus.getHandlerList(StockSetEvent.class).subscribe(listenerKey, this::onSet);

        eventBus.getHandlerList(PlayerStockHolderChangeEvent.class).subscribe(listenerKey, this::onStockHolderChange);
    }

    public void unregister() {
        if (listenerKey == null) {
            return;
        }

        var eventBus = BoxProvider.get().getEventBus();

        eventBus.getHandlerList(StockIncreaseEvent.class).unsubscribeAll(listenerKey);
        eventBus.getHandlerList(StockDecreaseEvent.class).unsubscribeAll(listenerKey);
        eventBus.getHandlerList(StockSetEvent.class).unsubscribeAll(listenerKey);

        eventBus.getHandlerList(PlayerStockHolderChangeEvent.class).unsubscribeAll(listenerKey);

        stockHolderMap.clear();
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
        if (event.isUserStockHolder()) {
            Optional.of(event.getStockHolder())
                    .map(StockHolder::getUUID)
                    .map(Bukkit::getPlayer)
                    .ifPresent(player -> player.sendActionBar(notification));
        } else {
            stockHolderMap.get(event.getStockHolder().getUUID())
                    .stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(player -> player.sendActionBar(notification));
        }
    }

    public void onStockHolderChange(@NotNull PlayerStockHolderChangeEvent event) {
        var playerUuid = event.getBoxPlayer().getUUID();

        var previousStockHolder = event.getPreviousStockHolder();
        var currentStockHolder = event.getBoxPlayer().getCurrentStockHolder();

        if (!(currentStockHolder instanceof UserStockHolder)) {
            addToMap(currentStockHolder.getUUID(), playerUuid);
        }

        if (!(previousStockHolder instanceof UserStockHolder)) {
            removeFromMap(previousStockHolder.getUUID(), playerUuid);
        }
    }

    private void addToMap(@NotNull UUID stockHolderUuid, @NotNull UUID playerUuid) {
        stockHolderMap.computeIfAbsent(stockHolderUuid, k -> new ArrayList<>()).add(playerUuid);
    }

    private void removeFromMap(@NotNull UUID stockHolderUuid, @NotNull UUID playerUuid) {
        var list = stockHolderMap.get(stockHolderUuid);

        if (list != null) {
            list.remove(playerUuid);
        }
    }
}
