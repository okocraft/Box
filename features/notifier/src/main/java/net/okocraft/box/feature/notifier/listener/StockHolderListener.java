package net.okocraft.box.feature.notifier.listener;

import com.github.siroshun09.event4j.key.Key;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.player.PlayerStockHolderChangeEvent;
import net.okocraft.box.api.event.player.PlayerUnloadEvent;
import net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockSetEvent;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.feature.notifier.factory.NotificationFactory;
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
        var playerMap = BoxProvider.get().getBoxPlayerMap();

        for (var player : Bukkit.getOnlinePlayers()) {
            if (playerMap.isLoaded(player)) {
                var stockHolder = playerMap.get(player).getCurrentStockHolder();

                if (!(stockHolder instanceof UserStockHolder)) {
                    addToMap(stockHolder.getUUID(), player.getUniqueId());
                }
            }
        }

        this.listenerKey = listenerKey;

        var eventBus = BoxProvider.get().getEventBus();

        eventBus.getSubscriber(StockIncreaseEvent.class).subscribe(listenerKey, this::onIncrease);
        eventBus.getSubscriber(StockDecreaseEvent.class).subscribe(listenerKey, this::onDecrease);
        eventBus.getSubscriber(StockSetEvent.class).subscribe(listenerKey, this::onSet);

        eventBus.getSubscriber(PlayerStockHolderChangeEvent.class).subscribe(listenerKey, this::onStockHolderChange);
        eventBus.getSubscriber(PlayerUnloadEvent.class).subscribe(listenerKey, this::onUnload);
    }

    public void unregister() {
        if (listenerKey == null) {
            return;
        }

        var eventBus = BoxProvider.get().getEventBus();

        eventBus.getSubscriber(StockIncreaseEvent.class).unsubscribeAll(listenerKey);
        eventBus.getSubscriber(StockDecreaseEvent.class).unsubscribeAll(listenerKey);
        eventBus.getSubscriber(StockSetEvent.class).unsubscribeAll(listenerKey);

        eventBus.getSubscriber(PlayerStockHolderChangeEvent.class).unsubscribeAll(listenerKey);
        eventBus.getSubscriber(PlayerUnloadEvent.class).unsubscribeAll(listenerKey);

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

    public void onUnload(@NotNull PlayerUnloadEvent event) {
        var player = event.getBoxPlayer();
        var stockHolder = player.getCurrentStockHolder();

        if (!(stockHolder instanceof UserStockHolder)) {
            removeFromMap(stockHolder.getUUID(), player.getUUID());
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
