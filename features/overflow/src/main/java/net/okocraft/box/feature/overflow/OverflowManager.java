package net.okocraft.box.feature.overflow;

import dev.siroshun.configapi.core.node.ListNode;
import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.core.node.Node;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.customdata.CustomDataManager;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

final class OverflowManager {

    private static final String CUSTOM_DATA_NAMESPACE = "overflow";
    private static final String HOLDERS_KEY = "holders";
    private static final String HOLDER_NAME = "Overflow";
    private static final StockEvent.Cause CAUSE = StockEvent.Cause.create("overflow");

    private final CustomDataManager customDataManager;
    private final StockStorage stockStorage;
    private final StockManager stockManager;
    private final ConcurrentMap<UUID, ReentrantLock> locks = new ConcurrentHashMap<>();

    OverflowManager(
        @NotNull CustomDataManager customDataManager,
        @NotNull StockStorage stockStorage,
        @NotNull StockManager stockManager
    ) {
        this.customDataManager = customDataManager;
        this.stockStorage = stockStorage;
        this.stockManager = stockManager;
    }

    void store(@NotNull UUID ownerUuid, @NotNull BoxItem item, int amount) throws Exception {
        if (amount <= 0) {
            return;
        }

        ReentrantLock lock = this.locks.computeIfAbsent(ownerUuid, _ -> new ReentrantLock());
        lock.lock();

        try {
            this.store0(ownerUuid, item, amount);
        } finally {
            lock.unlock();
        }
    }

    private void store0(@NotNull UUID ownerUuid, @NotNull BoxItem item, int amount) throws Exception {
        List<UUID> holderUuids = this.loadHolderUuids(ownerUuid);
        int remaining = amount;

        for (UUID holderUuid : holderUuids) {
            StockHolder holder = this.loadStockHolder(holderUuid);
            int capacity = Integer.MAX_VALUE - holder.getAmount(item);

            if (capacity <= 0) {
                continue;
            }

            int toStore = Math.min(remaining, capacity);
            holder.increase(item, toStore, CAUSE);
            this.saveStockHolder(holder, item);
            remaining -= toStore;

            if (remaining == 0) {
                return;
            }
        }

        UUID holderUuid;
        do {
            holderUuid = UUID.randomUUID();
        } while (holderUuids.contains(holderUuid));

        holderUuids.add(holderUuid);
        this.saveHolderUuids(ownerUuid, holderUuids);

        StockHolder holder = this.stockManager.createStockHolder(
            holderUuid,
            HOLDER_NAME,
            VoidStockEventCaller.INSTANCE
        );
        holder.increase(item, remaining, CAUSE);
        this.saveStockHolder(holder, item);
    }

    private @NotNull StockHolder loadStockHolder(@NotNull UUID holderUuid) throws Exception {
        Collection<StockData> stockData = this.stockStorage.loadStockData(holderUuid);
        return this.stockManager.createStockHolder(
            holderUuid,
            HOLDER_NAME,
            VoidStockEventCaller.INSTANCE,
            stockData
        );
    }

    private void saveStockHolder(@NotNull StockHolder holder, @NotNull BoxItem item) throws Exception {
        if (this.stockStorage instanceof PartialSavingStockStorage partialSaving) {
            partialSaving.savePartialStockData(
                holder.getUUID(),
                List.of(new StockData(item.getInternalId(), holder.getAmount(item)))
            );
        } else {
            this.stockStorage.saveStockData(holder.getUUID(), holder.toStockDataCollection());
        }
    }

    private @NotNull List<UUID> loadHolderUuids(@NotNull UUID ownerUuid) throws Exception {
        MapNode data = this.customDataManager.loadData(createCustomDataKey(ownerUuid));
        Node<?> holdersNode = data.get(HOLDERS_KEY);

        if (!(holdersNode instanceof ListNode holders)) {
            return new ArrayList<>();
        }

        List<UUID> result = new ArrayList<>();

        for (String value : holders.asList(String.class)) {
            try {
                result.add(UUID.fromString(value));
            } catch (IllegalArgumentException e) {
                BoxLogger.logger().warn(
                    "Ignoring invalid overflow stock holder UUID '{}' for {}.",
                    value,
                    ownerUuid
                );
            }
        }

        return result;
    }

    private void saveHolderUuids(@NotNull UUID ownerUuid, @NotNull List<UUID> holderUuids) throws Exception {
        MapNode data = MapNode.create();
        ListNode holders = data.createList(HOLDERS_KEY);
        holderUuids.forEach(uuid -> holders.add(uuid.toString()));
        this.customDataManager.saveData(createCustomDataKey(ownerUuid), data);
    }

    private static @NotNull Key createCustomDataKey(@NotNull UUID ownerUuid) {
        return Key.key(CUSTOM_DATA_NAMESPACE, ownerUuid.toString());
    }

    private enum VoidStockEventCaller implements StockEventCaller {
        INSTANCE;

        @Override
        public void callSetEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount, int previousAmount, StockEvent.@NotNull Cause cause) {
        }

        @Override
        public void callIncreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int currentAmount, StockEvent.@NotNull Cause cause) {
        }

        @Override
        public void callOverflowEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int increments, int excess, StockEvent.@NotNull Cause cause) {
        }

        @Override
        public void callDecreaseEvent(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int decrements, int currentAmount, StockEvent.@NotNull Cause cause) {
        }

        @Override
        public void callResetEvent(@NotNull StockHolder stockHolder, @NotNull Collection<StockData> stockDataBeforeReset) {
        }
    }
}
