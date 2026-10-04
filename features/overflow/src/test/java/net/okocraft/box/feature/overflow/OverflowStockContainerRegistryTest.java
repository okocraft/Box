package net.okocraft.box.feature.overflow;

import dev.siroshun.configapi.core.node.MapNode;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.model.customdata.CustomDataManager;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.core.model.stock.StockHolderFactory;
import net.okocraft.box.storage.api.model.stock.StockStorage;
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

class OverflowStockContainerRegistryTest {

    private static final DummyItem ITEM = new DummyItem(1, "dummy_item");

    @Test
    void testRetryAfterLoadFailure() throws Exception {
        MemoryCustomDataManager customDataManager = new MemoryCustomDataManager();
        MemoryStockStorage stockStorage = new MemoryStockStorage();
        OverflowStockContainerRegistry registry = createRegistry(customDataManager, stockStorage);
        UUID ownerUuid = UUID.randomUUID();

        customDataManager.failNextLoad();

        Assertions.assertThrows(
            Exception.class,
            () -> registry.increase(ownerUuid, ITEM, 10)
        );

        registry.save(ownerUuid);

        List<UUID> holderUuids = loadHolderUuids(customDataManager, ownerUuid);
        Assertions.assertEquals(1, holderUuids.size());
        Assertions.assertEquals(
            List.of(new StockData(ITEM.getInternalId(), 10)),
            stockStorage.loadStockData(holderUuids.getFirst())
        );
    }

    @Test
    void testRetryRemainderAfterRolloverSaveFailure() throws Exception {
        MemoryCustomDataManager customDataManager = new MemoryCustomDataManager();
        FailingStockStorage stockStorage = new FailingStockStorage();
        OverflowStockContainerRegistry registry = createRegistry(customDataManager, stockStorage);
        UUID ownerUuid = UUID.randomUUID();

        registry.increase(ownerUuid, ITEM, Integer.MAX_VALUE - 5);

        stockStorage.failNextSave();

        Assertions.assertThrows(
            OverflowStockContainer.PartialIncreaseException.class,
            () -> registry.increase(ownerUuid, ITEM, 10)
        );

        registry.save(ownerUuid);

        List<UUID> holderUuids = loadHolderUuids(customDataManager, ownerUuid);
        Assertions.assertEquals(2, holderUuids.size());
        Assertions.assertEquals(
            List.of(new StockData(ITEM.getInternalId(), Integer.MAX_VALUE)),
            stockStorage.loadStockData(holderUuids.getFirst())
        );
        Assertions.assertEquals(
            List.of(new StockData(ITEM.getInternalId(), 5)),
            stockStorage.loadStockData(holderUuids.getLast())
        );
    }

    @Test
    void testSaveAllPersistsLoadedOwnersWithoutUnloading() throws Exception {
        MemoryCustomDataManager customData = new MemoryCustomDataManager();
        MemoryStockStorage stocks = new MemoryStockStorage();
        OverflowStockContainerRegistry registry = createRegistry(customData, stocks);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        registry.increase(first, ITEM, 10);
        registry.increase(second, ITEM, 20);
        registry.saveAll();
        UUID firstHolder = loadHolderUuids(customData, first).getFirst();
        UUID secondHolder = loadHolderUuids(customData, second).getFirst();
        Assertions.assertEquals(List.of(new StockData(1, 10)), stocks.loadStockData(firstHolder));
        Assertions.assertEquals(List.of(new StockData(1, 20)), stocks.loadStockData(secondHolder));
        registry.increase(first, ITEM, 5);
        registry.saveAll();
        Assertions.assertEquals(List.of(firstHolder), loadHolderUuids(customData, first));
        Assertions.assertEquals(List.of(new StockData(1, 15)), stocks.loadStockData(firstHolder));
    }

    private static @NotNull OverflowStockContainerRegistry createRegistry(
        @NotNull CustomDataManager customDataManager,
        @NotNull StockStorage stockStorage
    ) {
        return new OverflowStockContainerRegistry(
            customDataManager,
            stockStorage,
            new TestStockManager(),
            StockEventCaller.createDefault(event -> {})
        );
    }

    private static @NotNull List<UUID> loadHolderUuids(
        @NotNull CustomDataManager customDataManager,
        @NotNull UUID ownerUuid
    ) throws Exception {
        return customDataManager.loadData(Key.key("overflow", ownerUuid.toString()))
            .getList("holders")
            .asList(String.class)
            .stream()
            .map(UUID::fromString)
            .toList();
    }

    private static final class MemoryCustomDataManager implements CustomDataManager {

        private final Map<Key, MapNode> data = new HashMap<>();
        private boolean failNextLoad;

        void failNextLoad() {
            this.failNextLoad = true;
        }

        @Override
        public @NotNull MapNode loadData(@NotNull Key key) throws Exception {
            if (this.failNextLoad) {
                this.failNextLoad = false;
                throw new Exception("Expected load failure");
            }

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

    private static final class FailingStockStorage implements StockStorage {

        private final MemoryStockStorage delegate = new MemoryStockStorage();
        private boolean failNextSave;

        void failNextSave() {
            this.failNextSave = true;
        }

        @Override
        public @NotNull Collection<StockData> loadStockData(@NotNull UUID uuid) throws Exception {
            return this.delegate.loadStockData(uuid);
        }

        @Override
        public void saveStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData) throws Exception {
            if (this.failNextSave) {
                this.failNextSave = false;
                throw new Exception("Expected save failure");
            }

            this.delegate.saveStockData(uuid, stockData);
        }

        @Override
        public void remapItemIds(@NotNull Int2IntMap remappedIdMap) throws Exception {
            this.delegate.remapItemIds(remappedIdMap);
        }

        @Override
        public Map<UUID, Collection<StockData>> loadAllStockData() throws Exception {
            return this.delegate.loadAllStockData();
        }

        @Override
        public void saveAllStockData(@NotNull Map<UUID, Collection<StockData>> stockDataMap) throws Exception {
            this.delegate.saveAllStockData(stockDataMap);
        }
    }

    private static final class TestStockManager implements StockManager {

        @Override
        public void saveAll() {
        }

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
