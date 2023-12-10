package net.okocraft.box.core.model.loader.state;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.storage.api.model.stock.PartialSavingStockStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Supplier;

public interface ChangeState {

    static @NotNull Supplier<ChangeState> createSupplier(@NotNull StockStorage storage) {
        if (storage instanceof PartialSavingStockStorage partialSaving) {
            return () -> new PerItemChangeState(partialSaving);
        } else {
            return () -> new BasicChangeState(storage);
        }
    }

    void rememberChange(int itemId);

    void rememberReset(@NotNull Collection<StockData> beforeReset);

    default boolean isInInterval(long saveIntervalNanos) {
        // Prevent overflow: System.nanoTime() < this.lastSave() + saveIntervalNanos
        return System.nanoTime() - saveIntervalNanos < this.lastSave();
    }

    long lastSave();

    void saveChanges(@NotNull StockHolder stockHolder) throws Exception;
}
