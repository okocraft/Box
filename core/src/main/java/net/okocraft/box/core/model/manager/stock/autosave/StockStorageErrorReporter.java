package net.okocraft.box.core.model.manager.stock.autosave;

import net.okocraft.box.api.model.stock.StockHolder;
import org.jetbrains.annotations.NotNull;

public interface StockStorageErrorReporter {

    void report(@NotNull StockHolder stockHolder, @NotNull Exception e);

}
