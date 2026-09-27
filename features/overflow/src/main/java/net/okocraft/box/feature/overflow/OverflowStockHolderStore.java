package net.okocraft.box.feature.overflow;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

final class OverflowStockHolderStore {

    private static final String HOLDER_NAME = "Overflow";
    private static final StockEvent.Cause CAUSE = StockEvent.Cause.create("overflow");
    private static final int LOCK_COUNT = 64;

    private final StockStorage stockStorage;
    private final StockManager stockManager;
    private final StockEventCaller eventCaller;
    private final ReentrantLock[] locks = new ReentrantLock[LOCK_COUNT];

    OverflowStockHolderStore(
        @NotNull StockStorage stockStorage,
        @NotNull StockManager stockManager,
        @NotNull StockEventCaller eventCaller
    ) {
        this.stockStorage = stockStorage;
        this.stockManager = stockManager;
        this.eventCaller = eventCaller;

        for (int i = 0; i < this.locks.length; i++) {
            this.locks[i] = new ReentrantLock();
        }
    }

    void increase(@NotNull UUID uuid, @NotNull BoxItem item, int amount) throws Exception {
        ReentrantLock lock = this.locks[uuid.hashCode() & (LOCK_COUNT - 1)];
        lock.lock();

        try {
            Collection<StockData> stockData = this.stockStorage.loadStockData(uuid);
            StockHolder holder = this.stockManager.createStockHolder(
                uuid,
                HOLDER_NAME,
                this.eventCaller,
                stockData
            );

            holder.increase(item, amount, CAUSE);
            this.save(holder, item);
        } finally {
            lock.unlock();
        }
    }

    private void save(@NotNull StockHolder holder, @NotNull BoxItem item) throws Exception {
        if (this.stockStorage instanceof PartialSavingStockStorage partialSaving) {
            partialSaving.savePartialStockData(
                holder.getUUID(),
                List.of(new StockData(item.getInternalId(), holder.getAmount(item)))
            );
        } else {
            this.stockStorage.saveStockData(holder.getUUID(), holder.toStockDataCollection());
        }
    }
}
