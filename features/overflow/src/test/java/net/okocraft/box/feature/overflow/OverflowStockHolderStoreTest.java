package net.okocraft.box.feature.overflow;

import net.okocraft.box.api.event.stockholder.stock.StockOverflowEvent;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.core.model.stock.StockHolderFactory;
import net.okocraft.box.test.shared.event.EventCollector;
import net.okocraft.box.test.shared.model.item.DummyItem;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryStockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

class OverflowStockHolderStoreTest {

    private static final DummyItem ITEM = new DummyItem(1, "dummy_item");

    @Test
    void testIncreaseAndOverflowEvent() throws Exception {
        MemoryStockStorage stockStorage = new MemoryStockStorage();
        EventCollector eventCollector = new EventCollector();
        OverflowStockHolderStore holderStore = new OverflowStockHolderStore(
            stockStorage,
            new TestStockManager(),
            StockEventCaller.createDefault(eventCollector.async())
        );
        UUID holderUuid = UUID.randomUUID();

        stockStorage.saveStockData(
            holderUuid,
            List.of(new StockData(ITEM.getInternalId(), Integer.MAX_VALUE - 5))
        );

        holderStore.increase(holderUuid, ITEM, 10);

        Assertions.assertEquals(
            List.of(new StockData(ITEM.getInternalId(), Integer.MAX_VALUE)),
            stockStorage.loadStockData(holderUuid)
        );
        eventCollector.checkEvent(StockOverflowEvent.class, event -> {
            Assertions.assertEquals(holderUuid, event.getStockHolder().getUUID());
            Assertions.assertEquals(ITEM, event.getItem());
            Assertions.assertEquals(5, event.getIncrements());
            Assertions.assertEquals(5, event.getExcess());
        });
    }

    private static final class TestStockManager implements StockManager {

        @Override
        public @NotNull PersonalStockHolder getPersonalStockHolder(@NotNull BoxUser user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull StockHolder createStockHolder(
            @NotNull UUID uuid,
            @NotNull String name,
            @NotNull StockEventCaller eventCaller
        ) {
            return this.createStockHolder(uuid, name, eventCaller, List.of());
        }

        @Override
        public @NotNull StockHolder createStockHolder(
            @NotNull UUID uuid,
            @NotNull String name,
            @NotNull StockEventCaller eventCaller,
            @NotNull Collection<StockData> stockData
        ) {
            return StockHolderFactory.create(
                uuid,
                name,
                eventCaller,
                stockData,
                id -> id == ITEM.getInternalId() ? ITEM : null
            );
        }
    }
}
