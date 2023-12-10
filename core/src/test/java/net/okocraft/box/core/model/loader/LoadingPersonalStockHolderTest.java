package net.okocraft.box.core.model.loader;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.core.model.loader.state.ChangeState;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.test.shared.model.item.DummyItem;
import net.okocraft.box.test.shared.model.stock.StockEventCollector;
import net.okocraft.box.test.shared.model.stock.TestStockHolder;
import net.okocraft.box.test.shared.model.user.TestUser;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryPartialSavingStockStorage;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryStockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

class LoadingPersonalStockHolderTest {

    private static final DummyItem ITEM_1 = new DummyItem(1, "test_item_1");
    private static final DummyItem ITEM_2 = new DummyItem(2, "test_item_2");
    private static final DummyItem ITEM_3 = new DummyItem(3, "test_item_3");

    @Test
    void testSavingWithBasicChangeState() throws Exception {
        testSaving(new MemoryStockStorage());
    }

    @Test
    void testSavingWithPerItemChangeState() throws Exception {
        testSaving(new MemoryPartialSavingStockStorage());
    }

    void testSaving(@NotNull StockStorage storage) throws Exception {
        var loader = createLoader(storage);
        addStock(loader);

        loader.saveChangesOrUnloadIfNeeded(Long.MAX_VALUE, 0);

        checkStockData(storage, loader);
    }

    @Test
    void testUnloadWithBasicChangeState() throws Exception {
        testUnload(new MemoryStockStorage());
    }

    @Test
    void testUnloadWithPerItemChangeState() throws Exception {
        testUnload(new MemoryPartialSavingStockStorage());
    }

    void testUnload(@NotNull StockStorage storage) throws Exception {
        var loader = createLoader(storage);

        Assertions.assertFalse(loader.isLoaded());

        loader.delegate(); // load stockholder
        Assertions.assertTrue(loader.isLoaded());

        addStock(loader);

        loader.saveChangesOrUnloadIfNeeded(0, 0);

        checkStockData(storage, loader);
        Assertions.assertFalse(loader.isLoaded()); // checks if the stockholder is unloaded
    }

    @Test
    void testNotUnloadingWhenOnline() throws Exception {
        var storage = new MemoryStockStorage();
        var loader = createLoader(storage);

        Assertions.assertFalse(loader.isLoaded());

        loader.delegate(); // load stockholder
        Assertions.assertTrue(loader.isLoaded());

        addStock(loader);

        loader.markAsOnline();

        loader.saveChangesOrUnloadIfNeeded(0, 0);
        checkStockData(storage, loader);
        Assertions.assertTrue(loader.isLoaded()); // checks if the stockholder is NOT unloaded

        loader.markAsOffline();

        loader.saveChangesOrUnloadIfNeeded(0, 0);
        checkStockData(storage, loader);
        Assertions.assertFalse(loader.isLoaded()); // checks if the stockholder is unloaded
    }

    @Test
    void testClose() {
        var storage = new MemoryStockStorage();
        var loader = createLoader(storage);

        Assertions.assertFalse(loader.isLoaded());
        Assertions.assertFalse(loader.isClosed());

        var loaded = loader.delegate(); // load stockholder
        Assertions.assertTrue(loader.isLoaded());
        Assertions.assertFalse(loader.isClosed());

        var unloaded = loader.close();
        Assertions.assertSame(loaded, unloaded);
        Assertions.assertFalse(loader.isLoaded());
        Assertions.assertTrue(loader.isClosed());

        Assertions.assertThrows(IllegalStateException.class, loader::delegate);
        Assertions.assertThrows(IllegalStateException.class, loader::load);
        Assertions.assertDoesNotThrow(loader::close);
        Assertions.assertDoesNotThrow(() -> loader.saveChangesOrUnloadIfNeeded(0, 0));
    }

    private static @NotNull LoadingPersonalStockHolder createLoader(@NotNull StockStorage storage) {
        return new LoadingPersonalStockHolder(TestUser.USER, ChangeState.createSupplier(storage).get(), loader -> TestStockHolder.create(new StateUpdater(loader.getChangeState())));
    }

    private static void addStock(@NotNull LoadingPersonalStockHolder loader) {
        // test_item_1: 10
        loader.increase(ITEM_1, 10, StockEventCollector.TEST_CAUSE);

        // test_item_2: 10
        loader.setAmount(ITEM_2, 10, StockEventCollector.TEST_CAUSE);

        // test_item_3: 5
        loader.setAmount(ITEM_3, 10, StockEventCollector.TEST_CAUSE);
        loader.decrease(ITEM_3, 5, StockEventCollector.TEST_CAUSE);
    }

    private static void checkStockData(@NotNull StockStorage storage, LoadingPersonalStockHolder loader) throws Exception {
        var expected = Set.of(new StockData(ITEM_1.internalId(), 10), new StockData(ITEM_2.internalId(), 10), new StockData(ITEM_3.internalId(), 5));
        var actual = storage.loadStockData(loader.getUUID());

        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertEquals(expected, Set.copyOf(actual));
    }

    private static class StateUpdater implements StockEventCaller {

        private final ChangeState state;

        private StateUpdater(@NotNull ChangeState state) {
            this.state = state;
        }

        @Override
        public void callSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, int previousAmount, StockEvent.@NotNull Cause cause) {
            this.state.rememberChange(item.getInternalId());
        }

        @Override
        public void callIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int currentAmount, StockEvent.@NotNull Cause cause) {
            this.state.rememberChange(item.getInternalId());
        }

        @Override
        public void callOverflowEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int excess, StockEvent.@NotNull Cause cause) {
            this.state.rememberChange(item.getInternalId());
        }

        @Override
        public void callDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int decrements, int currentAmount, StockEvent.@NotNull Cause cause) {
            this.state.rememberChange(item.getInternalId());
        }

        @Override
        public void callResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
            this.state.rememberReset(stockDataBeforeReset);
        }
    }
}
