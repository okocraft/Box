package net.okocraft.box.core.model.stock;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.test.shared.model.item.DummyItem;
import net.okocraft.box.test.shared.model.stock.StockEventCollector;
import net.okocraft.box.test.shared.model.stock.TestStockHolder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

class StockHolderTest {

    private static final DummyItem ITEM_1 = new DummyItem(1, "test_item_1");
    private static final DummyItem ITEM_2 = new DummyItem(2, "test_item_2");
    private static final DummyItem ITEM_3 = new DummyItem(3, "test_item_3");
    private static final IntFunction<BoxItem> TO_BOX_ITEM =
            id -> switch (id) {
                case 1 -> ITEM_1;
                case 2 -> ITEM_2;
                case 3 -> ITEM_3;
                default -> null;
            };

    @Test
    void testSet() {
        var collector = new StockEventCollector();
        var stockHolder = createStockHolder(collector);

        // Set the amount to 5 (new stock)
        // #getAmount: 5
        // Expected event:
        //   previous amount: 0
        //   current amount: 5
        stockHolder.setAmount(ITEM_1, 5, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(5, stockHolder.getAmount(ITEM_1));
        collector.checkSetEvent(stockHolder, ITEM_1, 0, 5);

        // Set the amount to 25 (existing stock, the amount is 5)
        // #getAmount: 25
        // Expected event:
        //   previous amount: 5
        //   current amount: 25
        stockHolder.setAmount(ITEM_1, 25, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(25, stockHolder.getAmount(ITEM_1));
        collector.checkSetEvent(stockHolder, ITEM_1, 5, 25);

        // Set the amount to 25 (same amount of stock)
        // #getAmount: 25
        // Expected event: not firing
        stockHolder.setAmount(ITEM_1, 25, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(25, stockHolder.getAmount(ITEM_1));
        collector.checkNoEvent();

        // Set the amount to 0 (existing stock, the amount is 25)
        // #getAmount: 0
        // Expected event:
        //   previous amount: 25
        //   current amount: 0
        stockHolder.setAmount(ITEM_1, 0, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_1));
        collector.checkSetEvent(stockHolder, ITEM_1, 25, 0);

        // Set the amount to 0 (new stock)
        // #getAmount: 0
        // Expected event: not firing
        stockHolder.setAmount(ITEM_2, 0, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_2));
        collector.checkNoEvent();
    }

    @Test
    void testIncrease() {
        var collector = new StockEventCollector();
        var stockHolder = createStockHolder(collector);

        // Increase the amount by 5 (new stock)
        // #getAmount and returning value: 5
        // Expected event:
        //   increments: 5
        //   current amount: 5
        Assertions.assertEquals(5, stockHolder.increase(ITEM_1, 5, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(5, stockHolder.getAmount(ITEM_1));
        collector.checkIncreaseEvent(stockHolder, ITEM_1, 5, 5);

        // Increase the amount by 15 (existing stock, the amount is 5)
        // #getAmount and returning value: 20
        // Expected event:
        //   increments: 15
        //   current amount: 20
        Assertions.assertEquals(20, stockHolder.increase(ITEM_1, 15, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(20, stockHolder.getAmount(ITEM_1));
        collector.checkIncreaseEvent(stockHolder, ITEM_1, 15, 20);

        // Increase the amount by 0 (existing stock, the amount is 20)
        // #getAmount and returning value: 20
        // Expected event: not firing
        Assertions.assertEquals(20, stockHolder.increase(ITEM_1, 0, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(20, stockHolder.getAmount(ITEM_1));
        collector.checkNoEvent();
    }

    @Test
    void testOverflow() {
        var collector = new StockEventCollector();
        var stockHolder = createStockHolder(collector);

        // Increase the amount by Integer#MAX_VALUE (new stock)
        // #getAmount and returning value: Integer#MAX_VALUE
        // Expected event:
        //   increments: Integer#MAX_VALUE
        //   current amount: Integer#MAX_VALUE
        Assertions.assertEquals(Integer.MAX_VALUE, stockHolder.increase(ITEM_1, Integer.MAX_VALUE, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(Integer.MAX_VALUE, stockHolder.getAmount(ITEM_1));
        collector.checkIncreaseEvent(stockHolder, ITEM_1, Integer.MAX_VALUE, Integer.MAX_VALUE);

        // Increase the amount by Integer#MAX_VALUE (existing stock, will be overflowed)
        // #getAmount and returning value: Integer#MAX_VALUE
        // Expected event:
        //   increments: 0
        //   current amount: Integer#MAX_VALUE
        //   excess: 10
        Assertions.assertEquals(Integer.MAX_VALUE, stockHolder.increase(ITEM_1, 10, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(Integer.MAX_VALUE, stockHolder.getAmount(ITEM_1));
        collector.checkOverflowEvent(stockHolder, ITEM_1, 0, 10);

        // Increase the amount by Integer#MAX_VALUE - 100 (new stock)
        // #getAmount and returning value: Integer#MAX_VALUE - 100
        // Expected event:
        //   increments: Integer#MAX_VALUE - 100
        //   current amount: Integer#MAX_VALUE - 100
        int amount = Integer.MAX_VALUE - 100;
        Assertions.assertEquals(amount, stockHolder.increase(ITEM_2, amount, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(amount, stockHolder.getAmount(ITEM_2));
        collector.checkIncreaseEvent(stockHolder, ITEM_2, amount, amount);

        // Increase the amount by 200 (existing stock, will be overflowed)
        // #getAmount and returning value: Integer#MAX_VALUE
        // Expected event:
        //   increments: 100
        //   current amount: Integer#MAX_VALUE
        //   excess: 100
        Assertions.assertEquals(Integer.MAX_VALUE, stockHolder.increase(ITEM_2, 200, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(Integer.MAX_VALUE, stockHolder.getAmount(ITEM_2));
        collector.checkOverflowEvent(stockHolder, ITEM_2, 100, 100);
    }

    @Test
    void testDecrease() {
        var collector = new StockEventCollector();
        var stockHolder = createStockHolder(collector);

        // Set the amount to 10 (new stock)
        // #getAmount: 10
        // Expected event:
        //   previous amount: 0
        //   current amount: 10
        stockHolder.setAmount(ITEM_1, 10, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(10, stockHolder.getAmount(ITEM_1));
        collector.checkSetEvent(stockHolder, ITEM_1, 0, 10);

        // Decrease the amount by 7 (existing stock, the amount is 10)
        // #getAmount and returning value: 3
        // Expected event:
        //   decrements: 7
        //   current amount: 3
        Assertions.assertEquals(3, stockHolder.decrease(ITEM_1, 7, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(3, stockHolder.getAmount(ITEM_1));
        collector.checkDecreaseEvent(stockHolder, ITEM_1, 7, 3);

        // Set the amount to 10 (new stock)
        // #getAmount: 10
        // Expected event:
        //   previous amount: 0
        //   current amount: 10
        stockHolder.setAmount(ITEM_2, 10, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(10, stockHolder.getAmount(ITEM_2));
        collector.checkSetEvent(stockHolder, ITEM_2, 0, 10);

        // Decrease the amount by 15 (existing stock, the amount is 10)
        // #getAmount and returning value: 0
        // Expected event:
        //   decrements: 10
        //   current amount: 0
        Assertions.assertEquals(0, stockHolder.decrease(ITEM_2, 15, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_2));
        collector.checkDecreaseEvent(stockHolder, ITEM_2, 10, 0);

        // Decrease the amount by 0 (existing stock, the amount is 3)
        // #getAmount and returning value: 3
        // Expected event: not firing
        Assertions.assertEquals(3, stockHolder.decrease(ITEM_1, 0, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(3, stockHolder.getAmount(ITEM_1));
        collector.checkNoEvent();

        // Decrease the amount by 10 (existing stock, but the amount is 0)
        // #getAmount and returning value: 0
        // Expected event: not firing
        Assertions.assertEquals(0, stockHolder.decrease(ITEM_2, 10, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_2));
        collector.checkNoEvent();

        // Decrease the amount by 10 (non-existing stock)
        // #getAmount and returning value: 0
        // Expected event: not firing
        Assertions.assertEquals(0, stockHolder.decrease(ITEM_3, 10, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_3));
        collector.checkNoEvent();
    }

    @Test
    void testDecreaseToZero() {
        var collector = new StockEventCollector();
        var stockHolder = createStockHolder(collector);

        // Set the amount to 10 (new stock)
        // #getAmount: 10
        // Expected event:
        //   previous amount: 0
        //   current amount: 10
        stockHolder.setAmount(ITEM_1, 10, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(10, stockHolder.getAmount(ITEM_1));
        collector.checkSetEvent(stockHolder, ITEM_1, 0, 10);

        // Decrease the amount by 7 (existing stock, the amount is 10)
        // Returning value: 7
        // #getAmount: 3
        // Expected event:
        //   decrements: 7
        //   current amount: 3
        Assertions.assertEquals(7, stockHolder.decreaseToZero(ITEM_1, 7, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(3, stockHolder.getAmount(ITEM_1));
        collector.checkDecreaseEvent(stockHolder, ITEM_1, 7, 3);

        // Set the amount to 10 (new stock)
        // #getAmount: 10
        // Expected event:
        //   previous amount: 0
        //   current amount: 10
        stockHolder.setAmount(ITEM_2, 10, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(10, stockHolder.getAmount(ITEM_2));
        collector.checkSetEvent(stockHolder, ITEM_2, 0, 10);

        // Decrease the amount by 15 (existing stock, the amount is 10)
        // Returning value: 10
        // #getAmount: 0
        // Expected event:
        //   decrements: 10
        //   current amount: 0
        Assertions.assertEquals(10, stockHolder.decreaseToZero(ITEM_2, 15, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_2));
        collector.checkDecreaseEvent(stockHolder, ITEM_2, 10, 0);

        // Decrease the amount by 0 (existing stock, the amount is 3)
        // Returning value: 0
        // #getAmount: 3
        // Expected event: not firing
        Assertions.assertEquals(0, stockHolder.decreaseToZero(ITEM_1, 0, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(3, stockHolder.getAmount(ITEM_1));
        collector.checkNoEvent();

        // Decrease the amount by 15 (existing stock, but the amount is zero)
        // Returning value: 0
        // #getAmount: 0
        // Expected event: not firing
        Assertions.assertEquals(0, stockHolder.decreaseToZero(ITEM_2, 15, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_2));
        collector.checkNoEvent();

        // Decrease the amount by 10 (non-existing stock)
        // Returning value: 0
        // #getAmount: 0
        // Expected event: not firing
        Assertions.assertEquals(0, stockHolder.decreaseToZero(ITEM_3, 10, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_3));
        collector.checkNoEvent();
    }

    @Test
    void testDecreaseSingleItemIfPossible() {
        var collector = new StockEventCollector();
        var stockHolder = createStockHolder(collector);

        // Set the amount to 10 (new stock)
        // #getAmount: 10
        // Expected event:
        //   previous amount: 0
        //   current amount: 10
        stockHolder.setAmount(ITEM_1, 10, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(10, stockHolder.getAmount(ITEM_1));
        collector.checkSetEvent(stockHolder, ITEM_1, 0, 10);

        // Decrease the amount by 7 (existing stock, the amount is 10)
        // #getAmount and returning value: 3
        // Expected event:
        //   decrements: 7
        //   current amount: 3
        Assertions.assertEquals(3, stockHolder.decreaseIfPossible(ITEM_1, 7, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(3, stockHolder.getAmount(ITEM_1));
        collector.checkDecreaseEvent(stockHolder, ITEM_1, 7, 3);

        // Decrease the amount by 0 (existing stock, the amount is 3)
        // Returning value: 3
        // #getAmount: 3
        // Expected event: not firing
        Assertions.assertEquals(3, stockHolder.decreaseIfPossible(ITEM_1, 0, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(3, stockHolder.getAmount(ITEM_1));
        collector.checkNoEvent();

        // Decrease the amount by 3 (existing stock, the amount is 3)
        // #getAmount and returning value: 0
        // Expected event:
        //   decrements: 3
        //   current amount: 0
        Assertions.assertEquals(0, stockHolder.decreaseIfPossible(ITEM_1, 3, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_1));
        collector.checkDecreaseEvent(stockHolder, ITEM_1, 3, 0);

        // Decrease the amount by 3 (existing stock, but the amount is zero)
        // Returning value: -1
        // #getAmount: 0
        // Expected event: not firing
        Assertions.assertEquals(-1, stockHolder.decreaseIfPossible(ITEM_1, 3, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_1));
        collector.checkNoEvent();

        // Set the amount to 10 (new stock)
        // #getAmount: 10
        // Expected event:
        //   previous amount: 0
        //   current amount: 10
        stockHolder.setAmount(ITEM_2, 10, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(10, stockHolder.getAmount(ITEM_2));
        collector.checkSetEvent(stockHolder, ITEM_2, 0, 10);

        // Decrease the amount by 15 (existing stock, but the amount is less than 15)
        // Returning value: -1
        // #getAmount: 10
        // Expected event: not firing
        Assertions.assertEquals(-1, stockHolder.decreaseIfPossible(ITEM_2, 15, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(10, stockHolder.getAmount(ITEM_2));
        collector.checkNoEvent();

        // Decrease the amount by 10 (non-existing stock)
        // Returning value: -1
        // #getAmount: 0
        // Expected event: not firing
        Assertions.assertEquals(-1, stockHolder.decreaseIfPossible(ITEM_3, 10, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_3));
        collector.checkNoEvent();
    }

    @Test
    void testDecreaseItemsIfPossible() {
        var collector = new StockEventCollector();
        var stockHolder = createStockHolder(collector);

        // Set the amount to 10 (new stock)
        // #getAmount: 10
        // Expected event:
        //   previous amount: 0
        //   current amount: 10
        stockHolder.setAmount(ITEM_1, 10, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(10, stockHolder.getAmount(ITEM_1));
        collector.checkSetEvent(stockHolder, ITEM_1, 0, 10);

        // Set the amount to 10 (new stock)
        // #getAmount: 10
        // Expected event:
        //   previous amount: 0
        //   current amount: 10
        stockHolder.setAmount(ITEM_2, 10, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(10, stockHolder.getAmount(ITEM_2));
        collector.checkSetEvent(stockHolder, ITEM_2, 0, 10);

        // Decrease test_item_1 by 5 and test_item_2 by 7
        // Returning value: true
        // #getAmount of test_item_1: 5
        // #getAmount of test_item_2: 3
        // Expected events:
        //   test_item_1:
        //     decrements: 5
        //     currentAmount: 5
        //   test_item_2:
        //     decrements: 7
        //     currentAmount: 3
        var fastUtilMap = new Object2IntLinkedOpenHashMap<BoxItem>();
        fastUtilMap.put(ITEM_1, 5);
        fastUtilMap.put(ITEM_2, 7);

        Assertions.assertTrue(stockHolder.decreaseIfPossible(fastUtilMap, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(5, stockHolder.getAmount(ITEM_1));
        Assertions.assertEquals(3, stockHolder.getAmount(ITEM_2));
        collector.checkDecreaseEvent(stockHolder, ITEM_1, 5, 5);
        collector.checkDecreaseEvent(stockHolder, ITEM_2, 7, 3);
        collector.checkNoEvent();

        // Decrease again
        // Returning value: false
        // #getAmount of test_item_1: 5
        // #getAmount of test_item_2: 3
        // Expected event: not firing
        Assertions.assertFalse(stockHolder.decreaseIfPossible(fastUtilMap, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(5, stockHolder.getAmount(ITEM_1));
        Assertions.assertEquals(3, stockHolder.getAmount(ITEM_2));
        collector.checkNoEvent();

        // Decrease test_item_1 by 3 and test_item_2 by 2
        // Returning value: true
        // #getAmount of test_item_1: 2
        // #getAmount of test_item_2: 1
        // Expected events:
        //   test_item_1:
        //     decrements: 3
        //     currentAmount: 2
        //   test_item_2:
        //     decrements: 2
        //     currentAmount: 1
        var javaMap = new LinkedHashMap<BoxItem, Integer>();
        javaMap.put(ITEM_1, 3);
        javaMap.put(ITEM_2, 2);

        Assertions.assertTrue(stockHolder.decreaseIfPossible(javaMap, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(2, stockHolder.getAmount(ITEM_1));
        Assertions.assertEquals(1, stockHolder.getAmount(ITEM_2));
        collector.checkDecreaseEvent(stockHolder, ITEM_1, 3, 2);
        collector.checkDecreaseEvent(stockHolder, ITEM_2, 2, 1);
        collector.checkNoEvent();

        // Decrease test_item_1, test_item_2, test_item_3 by 1
        // Returning value: false
        // #getAmount of test_item_1: 2
        // #getAmount of test_item_2: 1
        // #getAmount of test_item_3: 0
        // Expected events: not firing
        fastUtilMap.put(ITEM_1, 1);
        fastUtilMap.put(ITEM_2, 1);
        fastUtilMap.put(ITEM_3, 1);

        Assertions.assertFalse(stockHolder.decreaseIfPossible(fastUtilMap, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(2, stockHolder.getAmount(ITEM_1));
        Assertions.assertEquals(1, stockHolder.getAmount(ITEM_2));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_3));
        collector.checkNoEvent();

        // Decrease test_item_1 and test_item_2 by 1, and test_item_3 by 0
        // Returning value: true
        // #getAmount of test_item_1: 2
        // #getAmount of test_item_2: 1
        // #getAmount of test_item_3: 0
        // Expected events:
        //   test_item_1:
        //     decrements: 1
        //     currentAmount: 1
        //   test_item_2:
        //     decrements: 1
        //     currentAmount: 0
        fastUtilMap.put(ITEM_3, 0);

        Assertions.assertTrue(stockHolder.decreaseIfPossible(fastUtilMap, StockEventCollector.TEST_CAUSE));
        Assertions.assertEquals(1, stockHolder.getAmount(ITEM_1));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_2));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_3));
        collector.checkDecreaseEvent(stockHolder, ITEM_1, 1, 1);
        collector.checkDecreaseEvent(stockHolder, ITEM_2, 1, 0);
        collector.checkNoEvent();

        // Pass empty map
        // Returning value: true
        // Expected events: not firing
        Assertions.assertTrue(stockHolder.decreaseIfPossible(Object2IntMaps.emptyMap(), StockEventCollector.TEST_CAUSE));
        collector.checkNoEvent();

        // Pass empty map
        // Returning value: true
        // Expected events: not firing
        Assertions.assertTrue(stockHolder.decreaseIfPossible(Collections.emptyMap(), StockEventCollector.TEST_CAUSE));
        collector.checkNoEvent();
    }

    @Test
    void testReset() {
        var collector = new StockEventCollector();
        var stockHolder = createStockHolder(collector);

        // Set the amount to 10 (new stock)
        // #getAmount: 10
        // Expected event:
        //   previous amount: 0
        //   current amount: 10
        stockHolder.setAmount(ITEM_1, 10, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(10, stockHolder.getAmount(ITEM_1));
        collector.checkSetEvent(stockHolder, ITEM_1, 0, 10);

        // Set the amount to 10 (new stock)
        // #getAmount: 10
        // Expected event:
        //   previous amount: 0
        //   current amount: 10
        stockHolder.setAmount(ITEM_2, 10, StockEventCollector.TEST_CAUSE);
        Assertions.assertEquals(10, stockHolder.getAmount(ITEM_2));
        collector.checkSetEvent(stockHolder, ITEM_2, 0, 10);

        // Reset the stockholder
        // Expected event:
        //   stock data:
        //     - test_item_1: 10
        //     - test_item_2: 10
        stockHolder.reset();

        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_1));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_2));
        collector.checkResetEvent(stockHolder, List.of(new StockData(ITEM_1.internalId(), 10), new StockData(ITEM_2.internalId(), 10)));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testNullAndIllegalArguments() {
        var collector = new StockEventCollector();
        var stockHolder = createStockHolder(collector);

        Assertions.assertEquals(5, stockHolder.increase(ITEM_1, 5, StockEventCollector.TEST_CAUSE));
        collector.checkIncreaseEvent(stockHolder, ITEM_1, 5, 5);

        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.setAmount(null, 1, StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(IllegalArgumentException.class, () -> stockHolder.setAmount(ITEM_1, -1, StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.setAmount(ITEM_1, 1, null));

        Assertions.assertEquals(5, stockHolder.getAmount(ITEM_1));
        collector.checkNoEvent();

        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.increase(null, 1, StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(IllegalArgumentException.class, () -> stockHolder.increase(ITEM_1, -1, StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.increase(ITEM_1, 1, null));

        Assertions.assertEquals(5, stockHolder.getAmount(ITEM_1));
        collector.checkNoEvent();

        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.decrease(null, 1, StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(IllegalArgumentException.class, () -> stockHolder.decrease(ITEM_1, -1, StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.decrease(ITEM_1, 1, null));

        Assertions.assertEquals(5, stockHolder.getAmount(ITEM_1));
        collector.checkNoEvent();

        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.decreaseToZero(null, 1, StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(IllegalArgumentException.class, () -> stockHolder.decreaseToZero(ITEM_1, -1, StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.decreaseToZero(ITEM_1, 1, null));

        Assertions.assertEquals(5, stockHolder.getAmount(ITEM_1));
        collector.checkNoEvent();

        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.decreaseIfPossible(null, 1, StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(IllegalArgumentException.class, () -> stockHolder.decreaseIfPossible(ITEM_1, -1, StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.decreaseIfPossible(ITEM_1, 1, null));

        Assertions.assertEquals(5, stockHolder.getAmount(ITEM_1));
        collector.checkNoEvent();

        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.decreaseIfPossible(null, StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.decreaseIfPossible((Map<BoxItem, Integer>) null, StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.decreaseIfPossible(Object2IntMaps.emptyMap(), null));
        Assertions.assertThrows(NullPointerException.class, () -> stockHolder.decreaseIfPossible(Collections.emptyMap(), null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> stockHolder.decreaseIfPossible(new Object2IntArrayMap<>(Map.of(ITEM_1, -1)), StockEventCollector.TEST_CAUSE));
        Assertions.assertThrows(IllegalArgumentException.class, () -> stockHolder.decreaseIfPossible(Map.of(ITEM_1, -1), StockEventCollector.TEST_CAUSE));

        Assertions.assertEquals(5, stockHolder.getAmount(ITEM_1));
        Assertions.assertEquals(0, stockHolder.getAmount(ITEM_2));
    }

    private static @NotNull StockHolder createStockHolder(@NotNull StockEventCaller eventCaller) {
        return TestStockHolder.create(eventCaller, TO_BOX_ITEM);
    }
}
