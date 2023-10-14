package net.okocraft.box.core.model.manager.stock.autosave;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

class BasicChangeQueue implements ChangeQueue {

    private final StockStorage storage;
    private final StockHolder stockHolder;
    private final StockStorageErrorReporter reporter;
    private final AtomicBoolean hasChanges = new AtomicBoolean(false);

    BasicChangeQueue(@NotNull StockStorage storage, @NotNull StockHolder stockHolder, @NotNull StockStorageErrorReporter reporter) {
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
        this.hasChanges.set(true);
    }

    @Override
    public void rememberReset(@NotNull Collection<StockData> beforeReset) {
        this.hasChanges.set(true);
    }

    @Override
    public void saveChanges() {
        if (!this.hasChanges.compareAndSet(true, false)) {
            return;
        }

        try {
            this.storage.saveStockData(this.stockHolder.getUUID(), this.stockHolder.toStockDataCollection());
        } catch (Exception e) {
            this.reporter.report(this.stockHolder, e);
            this.hasChanges.set(true);
        }
    }

    @TestOnly
    boolean hasChanges() {
        return this.hasChanges.get();
    }
}
