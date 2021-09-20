package net.okocraft.box.core.task;

import com.github.siroshun09.event4j.handlerlist.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stock.StockEvent;
import net.okocraft.box.api.event.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stock.StockSetEvent;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.core.message.ErrorMessages;
import net.okocraft.box.core.storage.Storage;
import net.okocraft.box.core.util.executor.InternalExecutors;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ModifiedStockHolderSaveTask {

    private final Key listenerKey = Key.of(getClass().getSimpleName());
    private final List<UserStockHolder> modifiedStockHolders = new ArrayList<>();

    private final Storage storage;

    private ScheduledFuture<?> task;

    public ModifiedStockHolderSaveTask(@NotNull Storage storage) {
        this.storage = storage;
    }

    public void start() {
        var eventBus = BoxProvider.get().getEventBus();

        eventBus.getHandlerList(StockSetEvent.class).subscribe(listenerKey, this::processEvent);
        eventBus.getHandlerList(StockIncreaseEvent.class).subscribe(listenerKey, this::processEvent);
        eventBus.getHandlerList(StockDecreaseEvent.class).subscribe(listenerKey, this::processEvent);

        var scheduler = InternalExecutors.newSingleThreadScheduler("Auto-Save-Scheduler");
        task = scheduler.scheduleAtFixedRate(this::runTask, 10, 10, TimeUnit.MINUTES);
    }

    public void stop() {
        BoxProvider.get().getEventBus().unsubscribeAll(listenerKey);

        if (task != null) {
            task.cancel(true);
        }
    }

    private void processEvent(@NotNull StockEvent event) {
        if (event.isUserStockHolder()) {
            var userStockHolder = event.getUserStockHolder();

            if (!modifiedStockHolders.contains(userStockHolder)) {
                modifiedStockHolders.add(userStockHolder);
            }
        }
    }

    private void runTask() {
        BoxProvider.get().getLogger().info("Saving stock data... (" + modifiedStockHolders.size() + " players");
        var copied = List.copyOf(modifiedStockHolders);

        modifiedStockHolders.clear();

        var success =
                copied.stream()
                        .filter(UserStockHolder::isOnline)
                        .map(this::saveUserStockHolder)
                        .anyMatch(Boolean.FALSE::equals);

        if (success) {
            BoxProvider.get().getLogger().info("Player's stock data has been saved!");
        }
    }

    private boolean saveUserStockHolder(@NotNull UserStockHolder userStockHolder) {
        try {
            storage.getStockStorage().saveUserStockHolder(userStockHolder);
            return true;
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
            return false;
        }
    }
}
