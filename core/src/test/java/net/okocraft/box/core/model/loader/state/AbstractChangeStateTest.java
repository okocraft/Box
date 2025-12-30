package net.okocraft.box.core.model.loader.state;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.test.shared.model.stock.TestStockHolder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

abstract class AbstractChangeStateTest {

    protected static final int ITEM_ID = 1;
    protected static final int ITEM_AMOUNT = 10;
    protected static final List<StockData> STOCK_DATA = List.of(new StockData(ITEM_ID, ITEM_AMOUNT));

    @Test
    void testIsInInterval() throws Exception {
        long nanosToSave = TimeUnit.SECONDS.toNanos(15);
        ChangeState state = this.createState();
        Assertions.assertFalse(state.isInInterval(nanosToSave));
        state.saveChanges(TestStockHolder.create());
        Assertions.assertTrue(state.isInInterval(nanosToSave));
    }

    protected void checkStorageAmount(@NotNull StockStorage storage, @NotNull StockHolder stockHolder, int expectedAmount) {
        Collection<StockData> collection;

        try {
            collection = storage.loadStockData(stockHolder.getUUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (expectedAmount == 0) {
            Assertions.assertEquals(0, collection.size());
            return;
        }

        Assertions.assertEquals(1, collection.size());

        StockData data = collection.toArray(StockData[]::new)[0];

        Assertions.assertEquals(ITEM_ID, data.itemId());
        Assertions.assertEquals(expectedAmount, data.amount());
    }

    protected abstract @NotNull ChangeState createState();
}
