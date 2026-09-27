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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
    private final IntSet changedItemIds = new IntOpenHashSet();

    private StockHolder currentHolder;
    private boolean holderListChanged;

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

    void increase(@NotNull BoxItem item, int amount) throws Exception {
        if (amount <= 0) {
            return;
        }

        synchronized (this.lock) {
            StockHolder holder = this.getOrCreateCurrentHolder();
            int capacity = Integer.MAX_VALUE - holder.getAmount(item);

            if (amount <= capacity) {
                this.increase(holder, item, amount);
                return;
            }

            if (0 < capacity) {
                this.increase(holder, item, capacity);
            }

            // The current page will never be used again after moving to the next page.
            // Persist it here so only the latest page needs to stay in memory.
            this.saveChanges0();

            holder = this.createHolder();
            this.increase(holder, item, amount - capacity);
        }
    }

    void saveChanges() throws Exception {
        synchronized (this.lock) {
            this.saveChanges0();
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
            try {
                UUID holderUuid = UUID.fromString(value);

                if (loadedUuids.add(holderUuid)) {
                    this.holderUuids.add(holderUuid);
                }
            } catch (IllegalArgumentException e) {
                BoxLogger.logger().warn(
                    "Ignoring invalid overflow stock holder UUID '{}' for {}.",
                    value,
                    this.ownerUuid
                );
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

    private @NotNull StockHolder getOrCreateCurrentHolder() {
        return this.currentHolder != null ? this.currentHolder : this.createHolder();
    }

    private @NotNull StockHolder createHolder() {
        UUID holderUuid = UUID.randomUUID();
        this.holderUuids.add(holderUuid);
        this.holderListChanged = true;

        return this.currentHolder = this.stockManager.createStockHolder(
            holderUuid,
            HOLDER_NAME,
            this.eventCaller
        );
    }

    private void increase(@NotNull StockHolder holder, @NotNull BoxItem item, int amount) {
        holder.increase(item, amount, CAUSE);
        this.changedItemIds.add(item.getInternalId());
    }

    private void saveChanges0() throws Exception {
        if (this.currentHolder == null) {
            return;
        }

        if (!this.changedItemIds.isEmpty()) {
            this.saveCurrentHolder();
        }

        if (this.holderListChanged) {
            this.saveHolderList();
        }

        this.changedItemIds.clear();
        this.holderListChanged = false;
    }

    private void saveCurrentHolder() throws Exception {
        if (this.stockStorage instanceof PartialSavingStockStorage partialSaving) {
            List<StockData> stockData = new ArrayList<>(this.changedItemIds.size());

            for (int itemId : this.changedItemIds) {
                stockData.add(new StockData(itemId, this.currentHolder.getAmount(itemId)));
            }

            partialSaving.savePartialStockData(this.currentHolder.getUUID(), stockData);
        } else {
            this.stockStorage.saveStockData(
                this.currentHolder.getUUID(),
                this.currentHolder.toStockDataCollection()
            );
        }
    }

    private void saveHolderList() throws Exception {
        MapNode data = MapNode.create();
        ListNode holdersNode = data.createList(HOLDERS_KEY);

        for (UUID holderUuid : this.holderUuids) {
            holdersNode.add(holderUuid.toString());
        }

        this.customDataManager.saveData(createCustomDataKey(this.ownerUuid), data);
    }

    @SuppressWarnings("PatternValidation")
    private static @NotNull Key createCustomDataKey(@NotNull UUID ownerUuid) {
        return Key.key(CUSTOM_DATA_NAMESPACE, ownerUuid.toString());
    }
}
