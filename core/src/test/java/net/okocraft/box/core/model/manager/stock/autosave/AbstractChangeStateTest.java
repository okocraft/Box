package net.okocraft.box.core.model.manager.stock.autosave;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.test.shared.model.stock.TestStockHolder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.Collection;
import java.util.List;

abstract class AbstractChangeStateTest {

    protected static final int ITEM_ID = 1;
    protected static final int ITEM_AMOUNT = 10;
    protected static final List<StockData> STOCK_DATA = List.of(new StockData(ITEM_ID, ITEM_AMOUNT));

    @SuppressWarnings("unused")
    protected final void ignoreStorageError(@NotNull StockHolder stockHolder, @NotNull Exception e) {
    }

    protected @NotNull StockHolder createStockHolder() {
        return TestStockHolder.create(STOCK_DATA);
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

        var data = collection.toArray(StockData[]::new)[0];

        Assertions.assertEquals(ITEM_ID, data.itemId());
        Assertions.assertEquals(expectedAmount, data.amount());
    }
}
