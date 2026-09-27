package net.okocraft.box.feature.overflow;

import dev.siroshun.configapi.core.node.ListNode;
import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.core.node.Node;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class OverflowStockContainer {

    private static final String CUSTOM_DATA_NAMESPACE = "overflow";
    private static final String HOLDERS_KEY = "holders";
    private static final String HOLDER_NAME = "Overflow";
    private static final StockEvent.Cause CAUSE = StockEvent.Cause.create("overflow");

    private final UUID ownerUuid;
    private final CustomDataManager customDataManager;
    private final StockStorage stockStorage;
    private final StockManager stockManager;
    private final StockEventCaller eventCaller;

    private final Object lock = new Object();
    private final List<UUID> holderUuids = new ArrayList<>();
    private final Map<UUID, StockHolder> dirtyHolders = new HashMap<>();
    private final Map<UUID, IntSet> dirtyItems = new HashMap<>();

    private StockHolder currentHolder;
    private boolean holderListDirty;

    private OverflowStockContainer(
        @NotNull UUID ownerUuid,
        @NotNull CustomDataManager customDataManager,
        @NotNull StockStorage stockStorage,
        @NotNull StockManager stockManager,
        @NotNull StockEventCaller eventCaller
    ) {
        this.ownerUuid = ownerUuid;
        this.customDataManager = customDataManager;
        this.stockStorage = stockStorage;
        this.stockManager = stockManager;
        this.eventCaller = eventCaller;
    }

    static @NotNull OverflowStockContainer load(
        @NotNull UUID ownerUuid,
        @NotNull CustomDataManager customDataManager,
        @NotNull StockStorage stockStorage,
        @NotNull StockManager stockManager,
        @NotNull StockEventCaller eventCaller
    ) throws Exception {
        OverflowStockContainer container = new OverflowStockContainer(
            ownerUuid,
            customDataManager,
            stockStorage,
            stockManager,
            eventCaller
        );
        container.loadCurrentHolder();
        return container;
    }

    void increase(@NotNull BoxItem item, int amount) {
        if (amount <= 0) {
            return;
        }

        synchronized (this.lock) {
            StockHolder holder = this.currentHolderOrCreate();
            int capacity = Integer.MAX_VALUE - holder.getAmount(item);
            int increment = Math.min(amount, capacity);

            if (0 < increment) {
                holder.increase(item, increment, CAUSE);
                this.rememberChange(holder, item);
            }

            int remaining = amount - increment;

            if (0 < remaining) {
                holder = this.createHolder();
                holder.increase(item, remaining, CAUSE);
                this.rememberChange(holder, item);
            }
        }
    }

    void saveChanges() throws Exception {
        SaveSnapshot snapshot;

        synchronized (this.lock) {
            snapshot = this.createSaveSnapshot();

            if (snapshot == null) {
                return;
            }

            this.holderListDirty = false;
            this.dirtyHolders.clear();
            this.dirtyItems.clear();
        }

        try {
            for (DirtyHolder dirtyHolder : snapshot.dirtyHolders()) {
                this.saveHolder(dirtyHolder.holder(), dirtyHolder.itemIds());
            }

            if (snapshot.holderUuids() != null) {
                this.saveHolderList(snapshot.holderUuids());
            }
        } catch (Exception e) {
            synchronized (this.lock) {
                this.restore(snapshot);
            }
            throw e;
        }
    }

    private void loadCurrentHolder() throws Exception {
        MapNode data = this.customDataManager.loadData(createCustomDataKey(this.ownerUuid));
        Node<?> holdersNode = data.get(HOLDERS_KEY);

        if (!(holdersNode instanceof ListNode list)) {
            return;
        }

        Set<UUID> loadedUuids = new HashSet<>();

        for (String value : list.asList(String.class)) {
            UUID holderUuid;

            try {
                holderUuid = UUID.fromString(value);
            } catch (IllegalArgumentException e) {
                BoxLogger.logger().warn(
                    "Ignoring invalid overflow stock holder UUID '{}' for {}.",
                    value,
                    this.ownerUuid
                );
                continue;
            }

            if (loadedUuids.add(holderUuid)) {
                this.holderUuids.add(holderUuid);
            }
        }

        if (this.holderUuids.isEmpty()) {
            return;
        }

        UUID currentUuid = this.holderUuids.getLast();
        Collection<StockData> stockData = this.stockStorage.loadStockData(currentUuid);
        this.currentHolder = this.stockManager.createStockHolder(
            currentUuid,
            HOLDER_NAME,
            this.eventCaller,
            stockData
        );
    }

    private @NotNull StockHolder currentHolderOrCreate() {
        return this.currentHolder != null
            ? this.currentHolder
            : this.createHolder();
    }

    private @NotNull StockHolder createHolder() {
        UUID holderUuid = UUID.randomUUID();
        StockHolder holder = this.stockManager.createStockHolder(
            holderUuid,
            HOLDER_NAME,
            this.eventCaller
        );

        this.holderUuids.add(holderUuid);
        this.currentHolder = holder;
        this.holderListDirty = true;
        return holder;
    }

    private void rememberChange(@NotNull StockHolder holder, @NotNull BoxItem item) {
        this.dirtyHolders.put(holder.getUUID(), holder);
        this.dirtyItems
            .computeIfAbsent(holder.getUUID(), _ -> new IntOpenHashSet())
            .add(item.getInternalId());
    }

    private @Nullable SaveSnapshot createSaveSnapshot() {
        if (!this.holderListDirty && this.dirtyHolders.isEmpty()) {
            return null;
        }

        List<DirtyHolder> holders = new ArrayList<>(this.dirtyHolders.size());

        for (Map.Entry<UUID, StockHolder> entry : this.dirtyHolders.entrySet()) {
            IntSet itemIds = this.dirtyItems.get(entry.getKey());

            if (itemIds != null && !itemIds.isEmpty()) {
                holders.add(new DirtyHolder(entry.getValue(), new IntOpenHashSet(itemIds)));
            }
        }

        List<UUID> holderUuidSnapshot = this.holderListDirty
            ? List.copyOf(this.holderUuids)
            : null;

        return new SaveSnapshot(holders, holderUuidSnapshot);
    }

    private void restore(@NotNull SaveSnapshot snapshot) {
        for (DirtyHolder dirtyHolder : snapshot.dirtyHolders()) {
            UUID uuid = dirtyHolder.holder().getUUID();
            this.dirtyHolders.put(uuid, dirtyHolder.holder());
            this.dirtyItems
                .computeIfAbsent(uuid, _ -> new IntOpenHashSet())
                .addAll(dirtyHolder.itemIds());
        }

        if (snapshot.holderUuids() != null) {
            this.holderListDirty = true;
        }
    }

    private void saveHolder(@NotNull StockHolder holder, @NotNull IntSet itemIds) throws Exception {
        if (this.stockStorage instanceof PartialSavingStockStorage partialSaving) {
            List<StockData> stockData = new ArrayList<>(itemIds.size());

            for (int itemId : itemIds) {
                stockData.add(new StockData(itemId, holder.getAmount(itemId)));
            }

            partialSaving.savePartialStockData(holder.getUUID(), stockData);
        } else {
            this.stockStorage.saveStockData(holder.getUUID(), holder.toStockDataCollection());
        }
    }

    private void saveHolderList(@NotNull List<UUID> holderUuids) throws Exception {
        MapNode data = MapNode.create();
        ListNode holdersNode = data.createList(HOLDERS_KEY);

        for (UUID holderUuid : holderUuids) {
            holdersNode.add(holderUuid.toString());
        }

        this.customDataManager.saveData(createCustomDataKey(this.ownerUuid), data);
    }

    @SuppressWarnings("PatternValidation")
    private static @NotNull Key createCustomDataKey(@NotNull UUID ownerUuid) {
        return Key.key(CUSTOM_DATA_NAMESPACE, ownerUuid.toString());
    }

    private record DirtyHolder(@NotNull StockHolder holder, @NotNull IntSet itemIds) {
    }

    private record SaveSnapshot(
        @NotNull List<DirtyHolder> dirtyHolders,
        @Nullable List<UUID> holderUuids
    ) {
    }

}

