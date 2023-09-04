package net.okocraft.box.core.model.manager.stock.autosave;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.Collection;
import java.util.concurrent.locks.StampedLock;

class PartialSavingQueue implements ChangeQueue {

    private final PartialSavingStockStorage storage;
    private final StockHolder stockHolder;
    private final StockStorageErrorReporter reporter;

    private final IntSet itemIdSet = new IntOpenHashSet();
    private final StampedLock lock = new StampedLock();

    PartialSavingQueue(@NotNull PartialSavingStockStorage storage, @NotNull StockHolder stockHolder, @NotNull StockStorageErrorReporter reporter) {
        this.storage = storage;
        this.stockHolder = stockHolder;
        this.reporter = reporter;
    }

    @Override
    public @NotNull StockHolder getStockHolder() {
        return this.stockHolder;
    }

    @Override
    public void rememberChange(int itemId) {
        boolean isQueued;

        {
            long stamp = this.lock.tryOptimisticRead();
            isQueued = this.itemIdSet.contains(itemId);

            if (!this.lock.validate(stamp)) {
                long readStamp = this.lock.readLock();

                try {
                    isQueued = this.itemIdSet.contains(itemId);
                } finally {
                    this.lock.unlockRead(readStamp);
                }
            }
        }

        if (isQueued) {
            return;
        }

        long stamp = this.lock.writeLock();

        try {
            this.itemIdSet.add(itemId);
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    @Override
    public void rememberReset(@NotNull Collection<StockData> beforeReset) {
        long stamp = this.lock.writeLock();

        try {
            beforeReset.stream().mapToInt(StockData::itemId).forEach(this.itemIdSet::add);
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    @Override
    public void saveChanges() {
        {
            long stamp = this.lock.tryOptimisticRead();
            boolean empty = this.itemIdSet.isEmpty();

            if (this.lock.validate(stamp) && empty) {
                return;
            }
        }

        int[] itemIds;
        long stamp = this.lock.writeLock();

        try {
            itemIds = this.itemIdSet.toIntArray();
            this.itemIdSet.clear();
        } finally {
            this.lock.unlockWrite(stamp);
        }

        if (itemIds.length == 0) {
            return;
        }

        var partialStockData = new StockData[itemIds.length];

        for (int i = 0; i < itemIds.length; i++) {
            int itemId = itemIds[i];
            int amount = this.stockHolder.getAmount(itemId);

            partialStockData[i] = new StockData(itemId, amount);
        }

        try {
            this.storage.savePartialStockData(this.stockHolder.getUUID(), ObjectImmutableList.of(partialStockData));
        } catch (Exception e) {
            this.reporter.report(this.stockHolder, e);
        }
    }

    @TestOnly
    @NotNull IntSet getQueuedItemIds() {
        return this.itemIdSet;
    }
}
