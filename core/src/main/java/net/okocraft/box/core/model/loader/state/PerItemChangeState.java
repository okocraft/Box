package net.okocraft.box.core.model.loader.state;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
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

class PerItemChangeState implements ChangeState {

    private final PartialSavingStockStorage storage;

    private final IntSet itemIdSet = new IntOpenHashSet();
    private final StampedLock lock = new StampedLock();

    private long lastSave;

    PerItemChangeState(@NotNull PartialSavingStockStorage storage) {
        this.storage = storage;
    }

    @Override
    public void rememberChange(int itemId) {
        boolean alreadyChanged;

        {
            long stamp = this.lock.tryOptimisticRead();
            alreadyChanged = this.itemIdSet.contains(itemId);

            if (!this.lock.validate(stamp)) {
                long readStamp = this.lock.readLock();

                try {
                    alreadyChanged = this.itemIdSet.contains(itemId);
                } finally {
                    this.lock.unlockRead(readStamp);
                }
            }
        }

        if (alreadyChanged) {
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
    public long lastSave() {
        return this.lastSave;
    }

    @Override
    public void saveChanges(@NotNull StockHolder stockHolder) throws Exception {
        this.lastSave = System.nanoTime();

        {
            long stamp = this.lock.tryOptimisticRead();
            boolean empty = this.itemIdSet.isEmpty();

            if (this.lock.validate(stamp) && empty) {
                return;
            }
        }

        int[] itemIds;

        {
            long stamp = this.lock.writeLock();

            try {
                itemIds = this.itemIdSet.toIntArray();
                this.itemIdSet.clear();
            } finally {
                this.lock.unlockWrite(stamp);
            }
        }

        if (itemIds.length == 0) {
            return;
        }

        var partialStockData = new StockData[itemIds.length];

        for (int i = 0; i < itemIds.length; i++) {
            int itemId = itemIds[i];
            int amount = stockHolder.getAmount(itemId);

            partialStockData[i] = new StockData(itemId, amount);
        }

        try {
            this.storage.savePartialStockData(stockHolder.getUUID(), ObjectImmutableList.of(partialStockData));
        } catch (Exception e) {
            long stamp = this.lock.writeLock();

            try {
                this.itemIdSet.addAll(IntImmutableList.of(itemIds));
            } finally {
                this.lock.unlockWrite(stamp);
            }

            throw e;
        }
    }

    @TestOnly
    @NotNull IntSet getChangedItemIds() {
        return this.itemIdSet;
    }
}
