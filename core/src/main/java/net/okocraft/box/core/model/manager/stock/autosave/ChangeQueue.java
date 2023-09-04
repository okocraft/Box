package net.okocraft.box.core.model.manager.stock.autosave;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ChangeQueue {

    static @NotNull Factory createFactory(@NotNull StockStorage storage, @NotNull StockStorageErrorReporter reporter) {
        if (storage instanceof PartialSavingStockStorage partialSaving) {
            return stockHolder ->  new PartialSavingQueue(partialSaving, stockHolder, reporter);
        } else {
            return stockHolder -> new BasicChangeQueue(storage, stockHolder, reporter);
        }
    }

    @NotNull StockHolder getStockHolder();

    void rememberChange(int itemId);

    void rememberReset(@NotNull Collection<StockData> beforeReset);

    void saveChanges();

    interface Factory {
        @NotNull ChangeQueue createQueue(@NotNull StockHolder stockHolder);
    }
}
