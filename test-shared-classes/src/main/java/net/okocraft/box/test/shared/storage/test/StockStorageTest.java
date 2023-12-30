package net.okocraft.box.test.shared.storage.test;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class StockStorageTest {

    private static final UUID ID = UUID.randomUUID();
    private static final Collection<StockData> LAST_SAVED_DATA = List.of(stock(1, 1), stock(2, 2));

    public static void testLoadingAndSaving(@NotNull StockStorage storage) throws Exception {
        storage.init();

        Assertions.assertTrue(storage.loadStockData(ID).isEmpty()); // new player's stock data should be empty

        saveAndLoad(storage, List.of(stock(1, 1), stock(2, 2), stock(3, 3))); // initial saving
        saveAndLoad(storage, List.of(stock(1, 3), stock(2, 5))); // change amount and remove item #3
        saveAndLoad(storage, List.of(stock(1, 3), stock(2, 5), stock(4, 3))); // change amount and add item #4
        saveAndLoad(storage, LAST_SAVED_DATA); // save for next testing (testLoadingFromNewlyCreatedStorage)
    }

    public static void testLoadingFromNewlyCreatedStorage(@NotNull StockStorage storage) throws Exception {
        storage.init();
        checkStockData(LAST_SAVED_DATA, storage.loadStockData(ID));
    }

    public static void testPartialSaving(@NotNull PartialSavingStockStorage storage) throws Exception {
        storage.init();
        storage.savePartialStockData(ID, List.of(stock(1, 1)));
        checkStockData(List.of(stock(1, 1)), storage.loadStockData(ID));

        storage.savePartialStockData(ID, List.of(stock(2, 2)));
        checkStockData(List.of(stock(1, 1), stock(2, 2)), storage.loadStockData(ID));

        storage.savePartialStockData(ID, List.of(stock(3, 3), stock(1, 0)));
        checkStockData(List.of(stock(2, 2), stock(3, 3)), storage.loadStockData(ID));

        Assertions.assertTrue(storage.hasZeroStock());

        storage.cleanupZeroStockData();
        checkStockData(List.of(stock(2, 2), stock(3, 3)), storage.loadStockData(ID));

        Assertions.assertFalse(storage.hasZeroStock());
    }

    public static void testCleaningZeroStock(@NotNull StockStorage storage) throws Exception {
        storage.init();

        storage.saveStockData(ID, List.of(stock(1, 0), stock(2, 1)), Int2IntFunction.identity());
        checkStockData(List.of(stock(2, 1)), storage.loadStockData(ID));
    }

    private static void saveAndLoad(@NotNull StockStorage storage, @NotNull Collection<StockData> data) throws Exception {
        storage.saveStockData(ID, data, Int2IntFunction.identity());
        checkStockData(data, storage.loadStockData(ID));
    }

    private static void checkStockData(@NotNull Collection<StockData> expected, @NotNull Collection<StockData> actual) {
        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertTrue(actual.containsAll(expected));
    }

    private static @NotNull StockData stock(int id, int amount) {
        return new StockData(id, amount);
    }
}
