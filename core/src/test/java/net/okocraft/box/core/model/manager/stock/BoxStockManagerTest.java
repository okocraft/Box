package net.okocraft.box.core.model.manager.stock;

import net.okocraft.box.api.event.stockholder.StockHolderLoadEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.core.model.loader.state.ChangeState;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.test.shared.event.EventCollector;
import net.okocraft.box.test.shared.event.StockEventCollector;
import net.okocraft.box.test.shared.model.item.DummyItem;
import net.okocraft.box.test.shared.model.user.TestUser;
import net.okocraft.box.test.shared.scheduler.ScheduledTask;
import net.okocraft.box.test.shared.scheduler.TestScheduler;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryPartialSavingStockStorage;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryStockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
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
        Assertions.assertThrows(IllegalStateException.class, () -> manager.createStockHolder(UUID.randomUUID(), "unknown", StockEventCaller.createDefault(new EventCollector().sync())));
        Assertions.assertThrows(IllegalStateException.class, () -> manager.getPersonalStockHolder(TestUser.USER));
        Assertions.assertThrows(IllegalStateException.class, () -> manager.schedulerAutoSaveTask(new TestScheduler(false)));
        Assertions.assertThrows(IllegalStateException.class, manager::close);
    }

    @Test
    void testComputeInterval() {
        Assertions.assertEquals(TimeUnit.SECONDS.toNanos(1), BoxStockManager.computeInterval(0, 0));
        Assertions.assertEquals(3, BoxStockManager.computeInterval(0, 3));
        Assertions.assertEquals(3, BoxStockManager.computeInterval(3, 0));
        Assertions.assertEquals(3, BoxStockManager.computeInterval(3, 3));
        Assertions.assertEquals(6, BoxStockManager.computeInterval(12, 18));
    }

    @Test
    void testStateUpdatingStockEventCaller() {
        testStateUpdatingStockEventCaller(new MemoryStockStorage());
        testStateUpdatingStockEventCaller(new MemoryPartialSavingStockStorage());
    }

    private static void testStateUpdatingStockEventCaller(@NotNull StockStorage storage) {
        var eventCollector = new StockEventCollector();
        var manager = new BoxStockManager(storage, eventCollector, id -> null, 0, 0, TimeUnit.SECONDS);

        var loader = manager.getPersonalStockHolder(TestUser.USER);
        var stockEventCaller = manager.createStockEventCaller(loader);

        stockEventCaller.callSetEvent(loader, ITEM, 1, 0, StockEventCollector.TEST_CAUSE);
        eventCollector.checkSetEvent(loader, ITEM, 1, 0);
        checkChangeState(loader.getChangeState());

        stockEventCaller.callIncreaseEvent(loader, ITEM, 1, 2, StockEventCollector.TEST_CAUSE);
        eventCollector.checkIncreaseEvent(loader, ITEM, 1, 2);
        checkChangeState(loader.getChangeState());

        stockEventCaller.callOverflowEvent(loader, ITEM, 1, 1, StockEventCollector.TEST_CAUSE);
        eventCollector.checkOverflowEvent(loader, ITEM, 1, 1);
        checkChangeState(loader.getChangeState());

        stockEventCaller.callDecreaseEvent(loader, ITEM, 1, 1, StockEventCollector.TEST_CAUSE);
        eventCollector.checkDecreaseEvent(loader, ITEM, 1, 1);
        checkChangeState(loader.getChangeState());

        var stockData = List.of(new StockData(ITEM.getInternalId(), 1));
        stockEventCaller.callResetEvent(loader, stockData);
        eventCollector.checkResetEvent(loader, stockData);
        checkChangeState(loader.getChangeState());
    }

    private static void checkChangeState(@NotNull ChangeState state) {
        Assertions.assertTrue(state.forgetIfRemembered(ITEM.getInternalId()));
        Assertions.assertFalse(state.forgetIfRemembered(ITEM.getInternalId())); // Check if the id has cleared
    }

    @Test
    void testScheduleAutoSave() {
        var manager1 = new BoxStockManager(new MemoryStockStorage(), new EventCollector(), id -> null, 0, 0, TimeUnit.SECONDS);
        var scheduler = new TestScheduler(false);

        manager1.schedulerAutoSaveTask(scheduler);
        scheduler.checkTask(task -> {
            Assertions.assertEquals(task.delay(), Duration.ofSeconds(1));
            Assertions.assertEquals(task.interval(), Duration.ofSeconds(1));
            Assertions.assertEquals(task.type(), ScheduledTask.Type.ASYNC);
        });

        var manager2 = new BoxStockManager(new MemoryStockStorage(), new EventCollector(), id -> null, 12, 18, TimeUnit.SECONDS);

        manager2.schedulerAutoSaveTask(scheduler);
        scheduler.checkTask(task -> {
            Assertions.assertEquals(task.delay(), Duration.ofSeconds(6));
            Assertions.assertEquals(task.interval(), Duration.ofSeconds(6));
            Assertions.assertEquals(task.type(), ScheduledTask.Type.ASYNC);
        });
    }

    private static void checkStorageData(@NotNull MemoryStockStorage storage, int amount) {
        Assertions.assertEquals(List.of(new StockData(ITEM.internalId(), amount)), storage.loadStockData(TestUser.USER.getUUID()));
    }
}
