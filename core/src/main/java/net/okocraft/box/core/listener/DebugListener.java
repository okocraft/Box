package net.okocraft.box.core.listener;

import com.github.siroshun09.event4j.handlerlist.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.player.PlayerEvent;
import net.okocraft.box.api.event.player.PlayerStockHolderChangeEvent;
import net.okocraft.box.api.event.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stock.StockSetEvent;
import net.okocraft.box.api.event.stockholder.StockHolderEvent;
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
        if (event instanceof StockHolderEvent stockHolderEvent) {
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

            printLog(
                    stockHolderEvent.getEventName() + "{" +
                            "stockholderName='" + stockHolderEvent.getStockHolder().getName() + "', " +
                            "stockholderClass=" + stockHolderEvent.getStockHolder().getClass().getSimpleName() +
                            "}"
            );
            return;
        }

        if (event instanceof PlayerEvent playerEvent) {
            if (event instanceof PlayerStockHolderChangeEvent changeEvent) {
                printLog(changeEvent.getEventName() + "{" +
                        "playerUuid=" + changeEvent.getBoxPlayer().getUUID() + ", " +
                        "playerName='" + changeEvent.getBoxPlayer().getName() + "', " +
                        "previousStockHolderName='" + changeEvent.getPreviousStockHolder().getName() + "', " +
                        "previousStockHolderClass='" + changeEvent.getPreviousStockHolder().getClass().getSimpleName() + "'," +
                        "currentStockHolderName='" + changeEvent.getBoxPlayer().getName() + "', " +
                        "currentStockHolderClass='" + changeEvent.getBoxPlayer().getClass().getSimpleName() + "'" +
                        "}"
                );
                return;
            }

            printLog(playerEvent.getEventName() + "{" +
                    "playerUuid=" + playerEvent.getBoxPlayer().getUUID() + ", " +
                    "playerName='" + playerEvent.getBoxPlayer().getName() + "'" +
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
