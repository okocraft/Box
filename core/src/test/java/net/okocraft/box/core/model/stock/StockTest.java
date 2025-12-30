package net.okocraft.box.core.model.stock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StockTest {

    @Test
    void testInitialValue() {
        Assertions.assertEquals(10, new Stock(10).get());
    }

    @Test
    void testSet() {
        Stock stock = new Stock(10);
        int old = stock.set(5);
        Assertions.assertEquals(10, old);
        Assertions.assertEquals(5, stock.get());
    }

    @Test
    void testAdd() {
        Stock stock = new Stock(10);
        Stock.ModifyResult result = stock.add(5);
        Assertions.assertEquals(10, result.oldValue());
        Assertions.assertEquals(15, result.newValue());
    }

    @Test
    void testSubtract() {
        Stock stock = new Stock(10);
        Stock.ModifyResult result = stock.subtract(5);
        Assertions.assertEquals(10, result.oldValue());
        Assertions.assertEquals(5, result.newValue());

        Stock.ModifyResult notEnough = stock.subtract(10);
        Assertions.assertEquals(5, notEnough.oldValue());
        Assertions.assertEquals(0, notEnough.newValue());
    }

    @Test
    void testTrySubtract() {
        Stock stock = new Stock(10);
        Assertions.assertNull(stock.trySubtract(15));

        Stock.ModifyResult result = stock.trySubtract(5);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(10, result.oldValue());
        Assertions.assertEquals(5, result.newValue());
    }

    @Test
    void testOverflow() {
        Stock stock = new Stock(Integer.MAX_VALUE);
        Stock.ModifyResult.Overflow result = Assertions.assertInstanceOf(Stock.ModifyResult.Overflow.class, stock.add(100));

        Assertions.assertEquals(Integer.MAX_VALUE, result.oldValue());
        Assertions.assertEquals(Integer.MAX_VALUE, result.newValue());
        Assertions.assertEquals(100, result.excess());
    }
}
