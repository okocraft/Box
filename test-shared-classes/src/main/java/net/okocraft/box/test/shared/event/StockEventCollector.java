package net.okocraft.box.test.shared.event;

import net.okocraft.box.api.event.stockholder.StockHolderResetEvent;
import net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockOverflowEvent;
import net.okocraft.box.api.event.stockholder.stock.StockSetEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.Collection;

public class StockEventCollector extends EventCollector {

    public static final StockEvent.Cause TEST_CAUSE = StockEvent.Cause.create("BoxTestCause");

    public void checkSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int currentAmount, int previousAmount) {
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

    private void checkStockEvent(@NotNull StockEvent event, @NotNull StockHolder stockHolder, @NotNull BoxItem item, int currentAmount) {
        Assertions.assertEquals(stockHolder, event.getStockHolder());
        Assertions.assertEquals(item, event.getItem());
        Assertions.assertEquals(currentAmount, event.getAmount());
        Assertions.assertEquals(TEST_CAUSE, event.getCause());
    }

    public void checkResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
        var event = Assertions.assertInstanceOf(StockHolderResetEvent.class, this.nextEvent());

        Assertions.assertEquals(stockHolder, event.getStockHolder());
        Assertions.assertEquals(stockDataBeforeReset.size(), event.getStockDataBeforeReset().size());
        Assertions.assertTrue(stockDataBeforeReset.containsAll(event.getStockDataBeforeReset()));
    }
}
