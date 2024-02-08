package net.okocraft.box.core.model.loader.state;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

class BasicChangeState implements ChangeState {

    private final StockStorage storage;
    private final AtomicBoolean hasChanges = new AtomicBoolean(false);

    private volatile long lastSave;

    BasicChangeState(@NotNull StockStorage storage) {
        this.storage = storage;
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
    public long lastSave() {
        return this.lastSave;
    }

    @Override
    public void saveChanges(@NotNull StockHolder stockHolder) throws Exception {
        this.lastSave = System.nanoTime();

        if (!this.hasChanges.compareAndSet(true, false)) {
            return;
        }

        try {
            this.storage.saveStockData(stockHolder.getUUID(), stockHolder.toStockDataCollection(), Int2IntFunction.identity());
        } catch (Exception e) {
            this.hasChanges.set(true);
            throw e;
        }
    }

    @Override
    public boolean forgetIfRemembered(int itemId) {
        return this.hasChanges.compareAndSet(true, false);
    }

    @TestOnly
    boolean hasChanges() {
        return this.hasChanges.get();
    }
}
