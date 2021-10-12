package net.okocraft.box.core.listener;

import com.github.siroshun09.event4j.handlerlist.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.general.AutoSaveStartEvent;
import net.okocraft.box.api.event.player.PlayerUnloadEvent;
import net.okocraft.box.api.event.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stock.StockEvent;
import net.okocraft.box.api.event.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stock.StockSetEvent;
import net.okocraft.box.api.event.stockholder.StockHolderSaveEvent;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.core.message.ErrorMessages;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class StockHolderListener {

    private final Key listenerKey = Key.of(getClass().getSimpleName());
    private final List<UserStockHolder> modifiedStockHolders = new ArrayList<>();

    public void register() {
        var eventBus = BoxProvider.get().getEventBus();

        eventBus.getHandlerList(StockSetEvent.class).subscribe(listenerKey, this::enqueueStockHolder);
        eventBus.getHandlerList(StockIncreaseEvent.class).subscribe(listenerKey, this::enqueueStockHolder);
        eventBus.getHandlerList(StockDecreaseEvent.class).subscribe(listenerKey, this::enqueueStockHolder);

        eventBus.getHandlerList(StockHolderSaveEvent.class).subscribe(listenerKey, this::dequeueStockHolder);
        eventBus.getHandlerList(PlayerUnloadEvent.class).subscribe(listenerKey, this::dequeueStockHolder);

        eventBus.getHandlerList(AutoSaveStartEvent.class).subscribe(listenerKey, this::saveModifiedStockHolders);
    }

    public void unregister() {
        BoxProvider.get().getEventBus().unsubscribeAll(listenerKey);
    }

    private void enqueueStockHolder(@NotNull StockEvent event) {
        if (event.isUserStockHolder()) {
            var userStockHolder = event.getUserStockHolder();

            if (!modifiedStockHolders.contains(userStockHolder)) {
                modifiedStockHolders.add(userStockHolder);
            }
        }
    }

    private void dequeueStockHolder(@NotNull StockHolderSaveEvent event) {
        if (!modifiedStockHolders.isEmpty() && event.isUserStockHolder()) {
            modifiedStockHolders.remove(event.getUserStockHolder());
        }
    }

    private void dequeueStockHolder(@NotNull PlayerUnloadEvent event) {
        modifiedStockHolders.remove(event.getBoxPlayer().getUserStockHolder());
    }

    private void saveModifiedStockHolders(@NotNull AutoSaveStartEvent task) {
        if (modifiedStockHolders.isEmpty()) {
            return;
        }

        var copied = List.copyOf(modifiedStockHolders);

        modifiedStockHolders.clear();

        copied.forEach(this::save);
    }

    private void save(@NotNull UserStockHolder userStockHolder) {
        try {
            BoxProvider.get().getStockManager().saveUserStock(userStockHolder).join();
        } catch (Exception e) {
            Optional.of(userStockHolder.getUser())
                    .map(BoxUser::getUUID)
                    .map(Bukkit::getPlayer)
                    .ifPresent(player -> player.sendMessage(ErrorMessages.ERROR_SAVE_PLAYER_DATA));

            BoxProvider.get().getLogger().log(
                    Level.SEVERE,
                    "Could not save the user's stockholder (" + userStockHolder.getUser().getUUID() + ")",
                    e
            );
        }
    }
}
