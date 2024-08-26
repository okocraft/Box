package net.okocraft.box.api.model.stock;

import dev.siroshun.event4j.api.caller.EventCaller;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.stockholder.StockHolderResetEvent;
import net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockOverflowEvent;
import net.okocraft.box.api.event.stockholder.stock.StockSetEvent;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class DefaultStockEventCaller implements StockEventCaller {

    private final EventCaller<BoxEvent> caller;

    DefaultStockEventCaller(@NotNull EventCaller<BoxEvent> caller) {
        this.caller = caller;
    }

    @Override
    public void callSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, int previousAmount, StockEvent.@NotNull Cause cause) {
        this.caller.call(new StockSetEvent(stockHolder, item, amount, previousAmount, cause));
    }

    @Override
    public void callIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int currentAmount, StockEvent.@NotNull Cause cause) {
        this.caller.call(new StockIncreaseEvent(stockHolder, item, increments, currentAmount, cause));
    }

    @Override
    public void callOverflowEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int excess, StockEvent.@NotNull Cause cause) {
        this.caller.call(new StockOverflowEvent(stockHolder, item, increments, excess, cause));
    }

    @Override
    public void callDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int decrements, int currentAmount, StockEvent.@NotNull Cause cause) {
        this.caller.call(new StockDecreaseEvent(stockHolder, item, decrements, currentAmount, cause));
    }

    @Override
    public void callResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
        this.caller.call(new StockHolderResetEvent(stockHolder, stockDataBeforeReset));
    }
}
