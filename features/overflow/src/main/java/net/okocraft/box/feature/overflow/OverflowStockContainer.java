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

    private final List<StockHolder> holders = new ArrayList<>();
    private final Map<UUID, IntSet> dirtyItems = new HashMap<>();

    private boolean holderListDirty;

    private OverflowStockContainer(
        @NotNull UUID ownerUuid,
        @NotNull CustomDataManager customDataManager,
        @NotNull StockStorage stockStorage,
        @NotNull StockManager stockManager
    ) {
        this.ownerUuid = ownerUuid;
        this.customDataManager = customDataManager;
        this.stockStorage = stockStorage;
        this.stockManager = stockManager;
    }

    static @NotNull OverflowStockContainer load(
        @NotNull UUID ownerUuid,
        @NotNull CustomDataManager customDataManager,
        @NotNull StockStorage stockStorage,
        @NotNull StockManager stockManager
    ) throws Exception {
        OverflowStockContainer container = new OverflowStockContainer(
            ownerUuid,
            customDataManager,
            stockStorage,
            stockManager
        );
        container.loadHolders();
        return container;
    }

    synchronized void increase(@NotNull BoxItem item, int amount) {
        if (amount <= 0) {
            return;
        }

        StockHolder holder = this.lastHolderOrCreate();
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

    synchronized void saveChanges() throws Exception {
        if (this.holderListDirty) {
            this.saveHolderList();
            this.holderListDirty = false;
        }

        for (StockHolder holder : this.holders) {
            IntSet itemIds = this.dirtyItems.get(holder.getUUID());

            if (itemIds == null || itemIds.isEmpty()) {
                continue;
            }

            this.saveHolder(holder, itemIds);
            this.dirtyItems.remove(holder.getUUID());
        }
    }

    private void loadHolders() throws Exception {
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

            if (!loadedUuids.add(holderUuid)) {
                continue;
            }

            Collection<StockData> stockData = this.stockStorage.loadStockData(holderUuid);
            this.holders.add(
                this.stockManager.createStockHolder(
                    holderUuid,
                    HOLDER_NAME,
                    VoidStockEventCaller.INSTANCE,
                    stockData
                )
            );
        }
    }

    private @NotNull StockHolder lastHolderOrCreate() {
        return this.holders.isEmpty()
            ? this.createHolder()
            : this.holders.getLast();
    }

    private @NotNull StockHolder createHolder() {
        UUID holderUuid = UUID.randomUUID();
        StockHolder holder = this.stockManager.createStockHolder(
            holderUuid,
            HOLDER_NAME,
            VoidStockEventCaller.INSTANCE
        );

        this.holders.add(holder);
        this.holderListDirty = true;
        return holder;
    }

    private void rememberChange(@NotNull StockHolder holder, @NotNull BoxItem item) {
        this.dirtyItems
            .computeIfAbsent(holder.getUUID(), _ -> new IntOpenHashSet())
            .add(item.getInternalId());
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

    private void saveHolderList() throws Exception {
        MapNode data = MapNode.create();
        ListNode holdersNode = data.createList(HOLDERS_KEY);

        for (StockHolder holder : this.holders) {
            holdersNode.add(holder.getUUID().toString());
        }

        this.customDataManager.saveData(createCustomDataKey(this.ownerUuid), data);
    }

    @SuppressWarnings("PatternValidation")
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
