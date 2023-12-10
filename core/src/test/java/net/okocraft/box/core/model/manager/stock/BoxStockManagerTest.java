package net.okocraft.box.core.model.manager.stock;

import net.okocraft.box.api.event.stockholder.StockHolderLoadEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.test.shared.event.EventCollector;
import net.okocraft.box.test.shared.model.item.DummyItem;
import net.okocraft.box.test.shared.model.stock.StockEventCollector;
import net.okocraft.box.test.shared.model.user.TestUser;
import net.okocraft.box.test.shared.scheduler.TestScheduler;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryStockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

class BoxStockManagerTest {

    private static final DummyItem ITEM = new DummyItem(1, "test_item");

    @Test
    void testLoader() {
        var storage = new MemoryStockStorage();
        var eventCollector = new EventCollector();
        var manager = new BoxStockManager(storage, eventCollector, id -> null, 0, 0, TimeUnit.SECONDS);

        var loader = manager.getPersonalStockHolder(TestUser.USER);

        Assertions.assertSame(loader, manager.getPersonalStockHolder(TestUser.USER));

        loader.increase(ITEM, 1, StockEventCollector.TEST_CAUSE);

        eventCollector.checkEvent(StockHolderLoadEvent.class, event -> Assertions.assertSame(loader, event.getStockHolder()));
        eventCollector.checkEvent(StockIncreaseEvent.class, event -> Assertions.assertSame(loader, event.getStockHolder()));

        manager.autoSaveOrUnload(loader);

        checkStorageData(storage, 1);

        loader.increase(ITEM, 1, StockEventCollector.TEST_CAUSE);
        manager.closeLoader(loader);
        checkStorageData(storage, 2);
    }

    @Test
    void testClose() {
        var storage = new MemoryStockStorage();
        var eventCollector = new EventCollector();
        var manager = new BoxStockManager(storage, eventCollector, id -> null, 0, 0, TimeUnit.SECONDS);

        var loader = manager.getPersonalStockHolder(TestUser.USER);

        Assertions.assertSame(loader, manager.getPersonalStockHolder(TestUser.USER));

        loader.increase(ITEM, 1, StockEventCollector.TEST_CAUSE);

        eventCollector.checkEvent(StockHolderLoadEvent.class, event -> Assertions.assertSame(loader, event.getStockHolder()));
        eventCollector.checkEvent(StockIncreaseEvent.class, event -> Assertions.assertSame(loader, event.getStockHolder()));

        manager.close();

        checkStorageData(storage, 1);

        Assertions.assertThrows(IllegalStateException.class, () -> manager.getPersonalStockHolder(TestUser.USER));
        Assertions.assertThrows(IllegalStateException.class, () -> manager.createStockHolder(UUID.randomUUID(), "unknown", StockEventCaller.DEFAULT));
        Assertions.assertThrows(IllegalStateException.class, () -> manager.getPersonalStockHolder(TestUser.USER));
        Assertions.assertThrows(IllegalStateException.class, () -> manager.schedulerAutoSaveTask(new TestScheduler()));
        Assertions.assertThrows(IllegalStateException.class, manager::close);
    }

    private static void checkStorageData(@NotNull MemoryStockStorage storage, int amount) {
        Assertions.assertEquals(List.of(new StockData(ITEM.internalId(), amount)), storage.loadStockData(TestUser.USER.getUUID()));
    }
}
