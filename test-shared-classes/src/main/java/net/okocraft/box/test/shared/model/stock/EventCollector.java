package net.okocraft.box.test.shared.model.stock;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.stockholder.StockHolderResetEvent;
import net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockOverflowEvent;
import net.okocraft.box.api.event.stockholder.stock.StockSetEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.Collection;
import java.util.LinkedList;

public class EventCollector implements StockEventCaller {

    public static final StockEvent.Cause TEST_CAUSE = StockEvent.Cause.create("BoxTestCause");
    private final LinkedList<BoxEvent> events = new LinkedList<>();

    @Override
    public void callSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, int previousAmount, @NotNull StockEvent.Cause cause) {
        this.events.add(new StockSetEvent(stockHolder, item, amount, previousAmount, cause));
    }

    @Override
    public void callIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int currentAmount, @NotNull StockEvent.Cause cause) {
        this.events.add(new StockIncreaseEvent(stockHolder, item, increments, currentAmount, cause));
    }

    @Override
    public void callOverflowEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int excess, @NotNull StockEvent.Cause cause) {
        this.events.add(new StockOverflowEvent(stockHolder, item, increments, excess, cause));
    }

    @Override
    public void callDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int decrements, int currentAmount, @NotNull StockEvent.Cause cause) {
        this.events.add(new StockDecreaseEvent(stockHolder, item, decrements, currentAmount, cause));
    }

    @Override
    public void callResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
        this.events.add(new StockHolderResetEvent(stockHolder, stockDataBeforeReset));
    }

    public void checkSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int previousAmount, int currentAmount) {
        var event = Assertions.assertInstanceOf(StockSetEvent.class, this.nextEvent());

        Assertions.assertEquals(previousAmount, event.getPreviousAmount());
        this.checkStockEvent(event, stockHolder, item, currentAmount);
    }

    public void checkIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int currentAmount) {
        var event = Assertions.assertInstanceOf(StockIncreaseEvent.class, this.nextEvent());

        Assertions.assertEquals(increments, event.getIncrements());
        this.checkStockEvent(event, stockHolder, item, currentAmount);
    }

    public void checkOverflowEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int excess) {
        var event = Assertions.assertInstanceOf(StockOverflowEvent.class, this.nextEvent());

        Assertions.assertEquals(excess, event.getExcess());
        Assertions.assertEquals(increments, event.getIncrements());
        this.checkStockEvent(event, stockHolder, item, Integer.MAX_VALUE);
    }

    public void checkDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int decrements, int currentAmount) {
        var event = Assertions.assertInstanceOf(StockDecreaseEvent.class, this.nextEvent());

        Assertions.assertEquals(decrements, event.getDecrements());
        this.checkStockEvent(event, stockHolder, item, currentAmount);
    }

    public void checkStockEvent(@NotNull StockEvent event, @NotNull StockHolder stockHolder, @NotNull BoxItem item, int currentAmount) {
        Assertions.assertEquals(stockHolder, event.getStockHolder());
        Assertions.assertEquals(item, event.getItem());
        Assertions.assertEquals(currentAmount, event.getAmount());
        Assertions.assertEquals(TEST_CAUSE, event.getCause());
    }

    public void checkResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
        var event = Assertions.assertInstanceOf(StockHolderResetEvent.class, this.nextEvent());

        Assertions.assertEquals(stockHolder, event.getStockHolder());
        Assertions.assertTrue(stockDataBeforeReset.containsAll(event.getStockDataBeforeReset()));
    }

    public void checkNoEvent() {
        var event = this.events.poll();
        Assertions.assertNull(event);
    }

    private @NotNull BoxEvent nextEvent() {
        var event = this.events.poll();
        Assertions.assertNotNull(event);
        return event;
    }
}
