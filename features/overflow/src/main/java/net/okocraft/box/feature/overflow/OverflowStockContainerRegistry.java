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
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class OverflowStockContainerRegistry {

    private static final int OWNER_LOCK_COUNT = 64;

    private final CustomDataManager customDataManager;
    private final StockStorage stockStorage;
    private final StockManager stockManager;
    private final StockEventCaller eventCaller;
    private final Map<UUID, OverflowStockContainer> containers = new ConcurrentHashMap<>();
    private final Object[] ownerLocks = new Object[OWNER_LOCK_COUNT];
    private final ReentrantReadWriteLock lifecycleLock = new ReentrantReadWriteLock();

    private boolean closed;

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

        for (int i = 0; i < this.ownerLocks.length; i++) {
            this.ownerLocks[i] = new Object();
        }
    }

    void increase(@NotNull UUID ownerUuid, @NotNull BoxItem item, int amount) throws Exception {
        this.lifecycleLock.readLock().lock();

        try {
            this.checkOpen();

            synchronized (this.ownerLock(ownerUuid)) {
                this.getOrLoad(ownerUuid).increase(item, amount);
            }
        } finally {
            this.lifecycleLock.readLock().unlock();
        }
    }

    void saveIfLoaded(@NotNull UUID ownerUuid) throws Exception {
        this.lifecycleLock.readLock().lock();

        try {
            this.checkOpen();

            synchronized (this.ownerLock(ownerUuid)) {
                OverflowStockContainer container = this.containers.get(ownerUuid);

                if (container != null) {
                    container.saveChanges();
                }
            }
        } finally {
            this.lifecycleLock.readLock().unlock();
        }
    }

    void unload(@NotNull UUID ownerUuid) throws Exception {
        this.lifecycleLock.readLock().lock();

        try {
            this.checkOpen();

            synchronized (this.ownerLock(ownerUuid)) {
                OverflowStockContainer container = this.containers.get(ownerUuid);

                if (container != null) {
                    container.saveChanges();
                    this.containers.remove(ownerUuid, container);
                }
            }
        } finally {
            this.lifecycleLock.readLock().unlock();
        }
    }

    void close() {
        this.lifecycleLock.writeLock().lock();

        try {
            if (this.closed) {
                return;
            }

            this.closed = true;

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

            this.containers.clear();
        } finally {
            this.lifecycleLock.writeLock().unlock();
        }
    }

    private @NotNull OverflowStockContainer getOrLoad(@NotNull UUID ownerUuid) throws Exception {
        OverflowStockContainer loaded = this.containers.get(ownerUuid);

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

    private @NotNull Object ownerLock(@NotNull UUID ownerUuid) {
        return this.ownerLocks[ownerUuid.hashCode() & (OWNER_LOCK_COUNT - 1)];
    }

    private void checkOpen() {
        if (this.closed) {
            throw new IllegalStateException("This overflow stock container registry is already closed.");
        }
    }
}
