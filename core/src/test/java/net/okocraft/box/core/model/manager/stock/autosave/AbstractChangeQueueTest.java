package net.okocraft.box.core.model.manager.stock.autosave;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.stock.VoidStockEventCaller;
import net.okocraft.box.core.model.stock.StockHolderImpl;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

abstract class AbstractChangeQueueTest {

    protected static final int ITEM_ID = 1;
    protected static final int ITEM_AMOUNT = 10;

    @SuppressWarnings("unused")
    protected final void ignoreStorageError(@NotNull StockHolder stockHolder, @NotNull Exception e) {
    }

    protected @NotNull StockHolderImpl createStockHolder() {
        return (StockHolderImpl) StockHolderImpl.create(
                UUID.randomUUID(),
                getClass().getSimpleName(),
                VoidStockEventCaller.INSTANCE,
                createStockData()
        );
    }

    protected @NotNull Collection<StockData> createStockData() {
        return List.of(new StockData(ITEM_ID, ITEM_AMOUNT));
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
