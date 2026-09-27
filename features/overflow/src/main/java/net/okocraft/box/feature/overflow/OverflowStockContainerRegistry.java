package net.okocraft.box.feature.overflow;

import net.okocraft.box.api.model.customdata.CustomDataManager;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class OverflowStockContainerRegistry {

    private static final int LOAD_LOCK_COUNT = 64;

    private final CustomDataManager customDataManager;
    private final StockStorage stockStorage;
    private final StockManager stockManager;
    private final StockEventCaller eventCaller;
    private final Map<UUID, OverflowStockContainer> containers = new ConcurrentHashMap<>();
    private final Object[] loadLocks = new Object[LOAD_LOCK_COUNT];

    OverflowStockContainerRegistry(
        @NotNull CustomDataManager customDataManager,
        @NotNull StockStorage stockStorage,
        @NotNull StockManager stockManager,
        @NotNull StockEventCaller eventCaller
    ) {
        this.customDataManager = customDataManager;
        this.stockStorage = stockStorage;
        this.stockManager = stockManager;
        this.eventCaller = eventCaller;

        for (int i = 0; i < this.loadLocks.length; i++) {
            this.loadLocks[i] = new Object();
        }
    }

    void increase(@NotNull UUID ownerUuid, @NotNull BoxItem item, int amount) throws Exception {
        this.getOrLoad(ownerUuid).increase(item, amount);
    }

    void saveIfLoaded(@NotNull UUID ownerUuid) throws Exception {
        OverflowStockContainer container = this.containers.get(ownerUuid);

        if (container != null) {
            container.saveChanges();
        }
    }

    void unload(@NotNull UUID ownerUuid) throws Exception {
        OverflowStockContainer container = this.containers.get(ownerUuid);

        if (container == null) {
            return;
        }

        container.saveChanges();
        this.containers.remove(ownerUuid, container);
    }

    void saveAll() {
        for (Map.Entry<UUID, OverflowStockContainer> entry : this.containers.entrySet()) {
            try {
                entry.getValue().saveChanges();
            } catch (Exception e) {
                BoxLogger.logger().error(
                    "Could not save overflow stock for {}.",
                    entry.getKey(),
                    e
                );
            }
        }
    }

    private @NotNull OverflowStockContainer getOrLoad(@NotNull UUID ownerUuid) throws Exception {
        OverflowStockContainer loaded = this.containers.get(ownerUuid);

        if (loaded != null) {
            return loaded;
        }

        Object lock = this.loadLocks[ownerUuid.hashCode() & (LOAD_LOCK_COUNT - 1)];

        synchronized (lock) {
            loaded = this.containers.get(ownerUuid);

            if (loaded != null) {
                return loaded;
            }

            OverflowStockContainer container = OverflowStockContainer.load(
                ownerUuid,
                this.customDataManager,
                this.stockStorage,
                this.stockManager,
                this.eventCaller
            );
            this.containers.put(ownerUuid, container);
            return container;
        }
    }
}
