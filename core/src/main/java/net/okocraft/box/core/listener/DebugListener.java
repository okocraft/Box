package net.okocraft.box.core.listener;

import com.github.siroshun09.event4j.handlerlist.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.player.PlayerLoadEvent;
import net.okocraft.box.api.event.player.PlayerUnloadEvent;
import net.okocraft.box.api.event.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stock.StockSetEvent;
import net.okocraft.box.api.event.stockholder.StockHolderSaveEvent;
import org.jetbrains.annotations.NotNull;

public class DebugListener {

    private final Key listenerKey = Key.of("box:debug");

    public void register() {
        BoxProvider.get().getEventBus().getHandlerList(BoxEvent.class).subscribe(listenerKey, this::handleEvent);
    }

    public void unregister() {
        BoxProvider.get().getEventBus().getHandlerList(BoxEvent.class).unsubscribeAll(listenerKey);
    }

    private void handleEvent(@NotNull BoxEvent event) {
        if (event instanceof StockIncreaseEvent increaseEvent) {
            printLog(
                    increaseEvent.getEventName() + "{" +
                            "stockholderName='" + increaseEvent.getStockHolder().getName() + "', " +
                            "stockholderClass=" + increaseEvent.getStockHolder().getClass().getSimpleName() + ", " +
                            "boxItem=" + increaseEvent.getItem() + ", " +
                            "increments=" + increaseEvent.getIncrements() + ", " +
                            "current=" + increaseEvent.getAmount() +
                            "}"
            );
            return;
        }

        if (event instanceof StockDecreaseEvent decreaseEvent) {
            printLog(
                    decreaseEvent.getEventName() + "{" +
                            "stockholderName='" + decreaseEvent.getStockHolder().getName() + "', " +
                            "stockholderClass=" + decreaseEvent.getStockHolder().getClass().getSimpleName() + ", " +
                            "boxItem=" + decreaseEvent.getItem() + ", " +
                            "decrements=" + decreaseEvent.getDecrements() + ", " +
                            "current=" + decreaseEvent.getAmount() +
                            "}"
            );
            return;
        }

        if (event instanceof StockSetEvent setEvent) {
            printLog(
                    setEvent.getEventName() + "{" +
                            "stockholderName='" + setEvent.getStockHolder().getName() + "', " +
                            "stockholderClass=" + setEvent.getStockHolder().getClass().getSimpleName() + ", " +
                            "boxItem=" + setEvent.getItem() + ", " +
                            "current=" + setEvent.getAmount() +
                            "}"
            );
            return;
        }

        if (event instanceof StockHolderSaveEvent saveEvent) {
            printLog(
                    saveEvent.getEventName() + "{" +
                            "stockholderName='" + saveEvent.getStockHolder().getName() + "', " +
                            "stockholderClass=" + saveEvent.getStockHolder().getClass().getSimpleName() +
                            "}"
            );
            return;
        }

        if (event instanceof PlayerLoadEvent loadEvent) {
            printLog(loadEvent.getEventName() + "{" +
                    "playerUuid=" + loadEvent.getBoxPlayer().getUUID() + ", " +
                    "playerName='" + loadEvent.getBoxPlayer().getName() + "'" +
                    "}"
            );
            return;
        }

        if (event instanceof PlayerUnloadEvent unloadEvent) {
            printLog(unloadEvent.getEventName() + "{" +
                    "playerUuid=" + unloadEvent.getBoxPlayer().getUUID() + ", " +
                    "playerName='" + unloadEvent.getBoxPlayer().getName() + "'" +
                    "}"
            );
            return;
        }

        printLog(event.toString());
    }

    private void printLog(@NotNull String log) {
        BoxProvider.get().getLogger().info("DEBUG: " + log);
    }
}
