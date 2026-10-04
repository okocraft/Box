package net.okocraft.box.feature.overflow;

import net.okocraft.box.api.model.customdata.CustomDataManager;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class OverflowStockContainerRegistry {

    private static final int OWNER_LOCK_COUNT = 64;
    private static final BigInteger MAX_INCREMENT = BigInteger.valueOf(Integer.MAX_VALUE);

    private final CustomDataManager customDataManager;
    private final StockStorage stockStorage;
    private final StockManager stockManager;
    private final StockEventCaller eventCaller;
    private final Map<UUID, OverflowStockContainer> containers = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Integer, PendingOverflow>> pendingOverflows = new ConcurrentHashMap<>();
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
                this.addPending(ownerUuid, item, amount);
                this.applyPending(ownerUuid);
            }
        } finally {
            this.lifecycleLock.readLock().unlock();
        }
    }

    void save(@NotNull UUID ownerUuid) throws Exception {
        this.lifecycleLock.readLock().lock();

        try {
            this.checkOpen();

            synchronized (this.ownerLock(ownerUuid)) {
                this.applyPending(ownerUuid);

                OverflowStockContainer container = this.containers.get(ownerUuid);

                if (container != null) {
                    container.saveChanges();
                }
            }
        } finally {
            this.lifecycleLock.readLock().unlock();
        }
    }

    void saveAll() throws Exception {
        this.lifecycleLock.readLock().lock();
        try {
            this.checkOpen();
            Set<UUID> ownerUuids = new HashSet<>(this.containers.keySet());
            ownerUuids.addAll(this.pendingOverflows.keySet());
            for (UUID ownerUuid : ownerUuids) {
                this.save(ownerUuid);
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
                this.applyPending(ownerUuid);

                OverflowStockContainer container = this.containers.get(ownerUuid);

                if (container != null) {
                    container.saveChanges();
                    this.containers.remove(ownerUuid, container);
                }

                this.pendingOverflows.remove(ownerUuid);
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

            Set<UUID> ownerUuids = new HashSet<>(this.containers.keySet());
            ownerUuids.addAll(this.pendingOverflows.keySet());

            for (UUID ownerUuid : ownerUuids) {
                try {
                    this.applyPending(ownerUuid);

                    OverflowStockContainer container = this.containers.get(ownerUuid);

                    if (container != null) {
                        container.saveChanges();
                    }
                } catch (Exception e) {
                    BoxLogger.logger().error(
                        "Could not save overflow stock for {}.",
                        ownerUuid,
                        e
                    );
                }
            }

            this.containers.clear();
            this.pendingOverflows.clear();
        } finally {
            this.lifecycleLock.writeLock().unlock();
        }
    }

    private void addPending(@NotNull UUID ownerUuid, @NotNull BoxItem item, int amount) {
        if (amount <= 0) {
            return;
        }

        Map<Integer, PendingOverflow> pendingByItem =
            this.pendingOverflows.computeIfAbsent(ownerUuid, _ -> new HashMap<>());

        pendingByItem.compute(
            item.getInternalId(),
            (_, pending) -> new PendingOverflow(
                item,
                BigInteger.valueOf(amount).add(
                    pending != null ? pending.amount() : BigInteger.ZERO
                )
            )
        );
    }

    private void applyPending(@NotNull UUID ownerUuid) throws Exception {
        Map<Integer, PendingOverflow> pendingByItem = this.pendingOverflows.get(ownerUuid);

        if (pendingByItem == null || pendingByItem.isEmpty()) {
            return;
        }

        OverflowStockContainer container = this.getOrLoad(ownerUuid);
        Iterator<Map.Entry<Integer, PendingOverflow>> iterator = pendingByItem.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, PendingOverflow> entry = iterator.next();
            PendingOverflow pending = entry.getValue();
            BigInteger remaining = pending.amount();

            while (0 < remaining.signum()) {
                int amount = remaining.min(MAX_INCREMENT).intValueExact();

                try {
                    container.increase(pending.item(), amount);
                    remaining = remaining.subtract(BigInteger.valueOf(amount));
                } catch (OverflowStockContainer.PartialIncreaseException e) {
                    if (0 < e.increased()) {
                        remaining = remaining.subtract(BigInteger.valueOf(e.increased()));
                    }

                    if (remaining.signum() == 0) {
                        iterator.remove();
                    } else {
                        entry.setValue(new PendingOverflow(pending.item(), remaining));
                    }

                    throw e;
                }
            }

            iterator.remove();
        }

        if (pendingByItem.isEmpty()) {
            this.pendingOverflows.remove(ownerUuid, pendingByItem);
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

    private record PendingOverflow(@NotNull BoxItem item, @NotNull BigInteger amount) {
    }
}
