package net.okocraft.box.test.shared.storage.test;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class StockStorageTest<S> extends AbstractStorageTest<S> {

    private static final UUID ID = UUID.randomUUID();

    private static void saveAndLoad(@NotNull StockStorage storage, @NotNull Collection<StockData> data) throws Exception {
        storage.saveStockData(ID, data);
        checkStockData(data, storage.loadStockData(ID));
    }

    private static void checkStockData(@NotNull Collection<StockData> expected, @NotNull Collection<StockData> actual) {
        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertTrue(actual.containsAll(expected));
    }

    private static @NotNull StockData stock(int id, int amount) {
        return new StockData(id, amount);
    }

    @Test
    void testLoadingAndSaving() throws Exception {
        var storage = this.newStorage();
        var stockStorage = this.newStockStorage(storage);

        try {
            Assertions.assertTrue(stockStorage.loadStockData(ID).isEmpty()); // new player's stock data should be empty

            saveAndLoad(stockStorage, List.of(stock(1, 1), stock(2, 2), stock(3, 3))); // initial saving
            saveAndLoad(stockStorage, List.of(stock(1, 3), stock(2, 5))); // change amount and remove item #3
            saveAndLoad(stockStorage, List.of(stock(1, 3), stock(2, 5), stock(4, 3))); // change amount and add item #4
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testPartialSaving() throws Exception {
        var storage = this.newStorage();

        if (!(this.newStockStorage(storage) instanceof PartialSavingStockStorage stockStorage)) {
            this.closeStorage(storage);
            return;
        }

        try {
            stockStorage.savePartialStockData(ID, List.of(stock(1, 1)));
            checkStockData(List.of(stock(1, 1)), stockStorage.loadStockData(ID));

            stockStorage.savePartialStockData(ID, List.of(stock(2, 2)));
            checkStockData(List.of(stock(1, 1), stock(2, 2)), stockStorage.loadStockData(ID));

            stockStorage.savePartialStockData(ID, List.of(stock(3, 3), stock(1, 0)));
            checkStockData(List.of(stock(2, 2), stock(3, 3)), stockStorage.loadStockData(ID));

            Assertions.assertTrue(stockStorage.hasZeroStock());

            stockStorage.cleanupZeroStockData();
            checkStockData(List.of(stock(2, 2), stock(3, 3)), stockStorage.loadStockData(ID));

            Assertions.assertFalse(stockStorage.hasZeroStock());
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testCleaningZeroStock() throws Exception {
        var storage = this.newStorage();
        var stockStorage = this.newStockStorage(storage);

        try {
            stockStorage.saveStockData(ID, List.of(stock(1, 0), stock(2, 1)));
            checkStockData(List.of(stock(2, 1)), stockStorage.loadStockData(ID));
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testRemapItemId() throws Exception {
        var storage = this.newStorage();
        var stockStorage = this.newStockStorage(storage);

        try {
            saveAndLoad(stockStorage, List.of(stock(1, 1), stock(2, 2), stock(3, 3))); // initial saving
            stockStorage.remapItemIds(new Int2IntArrayMap(new int[]{1, 2}, new int[]{5, 3})); // 1 -> 5 (new id), 2 -> 3 (merge)
            checkStockData(List.of(stock(5, 1), stock(3, 5)), stockStorage.loadStockData(ID));
        } finally {
            this.closeStorage(storage);
        }
    }

    protected abstract @NotNull StockStorage newStockStorage(@NotNull S storage) throws Exception;

}
