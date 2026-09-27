package net.okocraft.box.feature.overflow;

import dev.siroshun.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.model.customdata.CustomDataManager;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.core.model.stock.StockHolderFactory;
import net.okocraft.box.test.shared.model.item.DummyItem;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryStockStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

class OverflowStockContainerTest {

    private static final DummyItem ITEM = new DummyItem(1, "dummy_item");

    @Test
    void testIncreaseDoesNotWriteUntilSave() throws Exception {
        MemoryCustomDataManager customDataManager = new MemoryCustomDataManager();
        MemoryStockStorage stockStorage = new MemoryStockStorage();
        UUID ownerUuid = UUID.randomUUID();

        OverflowStockContainer container = OverflowStockContainer.load(
            ownerUuid,
            customDataManager,
            stockStorage,
            new TestStockManager()
        );

        container.increase(ITEM, 10);

        Assertions.assertTrue(stockStorage.loadAllStockData().isEmpty());
        Assertions.assertTrue(
            customDataManager.loadData(Key.key("overflow", ownerUuid.toString())).isEmpty()
        );

        container.saveChanges();

        MapNode data = customDataManager.loadData(Key.key("overflow", ownerUuid.toString()));
        UUID holderUuid = UUID.fromString(data.getList("holders").asList(String.class).getFirst());

        Assertions.assertEquals(
            List.of(new StockData(ITEM.getInternalId(), 10)),
            stockStorage.loadStockData(holderUuid)
        );
    }

    @Test
    void testCreateNextPageWhenLastPageIsFull() throws Exception {
        MemoryCustomDataManager customDataManager = new MemoryCustomDataManager();
        MemoryStockStorage stockStorage = new MemoryStockStorage();
        UUID ownerUuid = UUID.randomUUID();

        OverflowStockContainer container = OverflowStockContainer.load(
            ownerUuid,
            customDataManager,
            stockStorage,
            new TestStockManager()
        );

        container.increase(ITEM, Integer.MAX_VALUE);
        container.saveChanges();
        container.increase(ITEM, 10);
        container.saveChanges();

        MapNode data = customDataManager.loadData(Key.key("overflow", ownerUuid.toString()));
        List<UUID> holderUuids = data.getList("holders")
            .asList(String.class)
            .stream()
            .map(UUID::fromString)
            .toList();

        Assertions.assertEquals(2, holderUuids.size());
        Assertions.assertEquals(
            List.of(new StockData(ITEM.getInternalId(), Integer.MAX_VALUE)),
            stockStorage.loadStockData(holderUuids.getFirst())
        );
        Assertions.assertEquals(
            List.of(new StockData(ITEM.getInternalId(), 10)),
            stockStorage.loadStockData(holderUuids.getLast())
        );
    }

    private static final class MemoryCustomDataManager implements CustomDataManager {

        private final Map<Key, MapNode> data = new HashMap<>();

        @Override
        public @NotNull MapNode loadData(@NotNull Key key) {
            MapNode node = this.data.get(key);
            return node != null ? node.copy() : MapNode.create();
        }

        @Override
        public void saveData(@NotNull Key key, @NotNull MapNode mapNode) {
            this.data.put(key, mapNode.copy());
        }

        @Override
        public void visitData(@NotNull String namespace, @NotNull BiConsumer<Key, MapNode> consumer) {
            this.data.forEach((key, node) -> {
                if (key.namespace().equals(namespace)) {
                    consumer.accept(key, node.copy());
                }
            });
        }
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
