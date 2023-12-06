package net.okocraft.box.storage.memory;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.registry.StorageContext;
import net.okocraft.box.storage.memory.stock.MemoryPartialSavingStockStorage;
import net.okocraft.box.storage.memory.stock.MemoryStockStorage;
import net.okocraft.box.storage.memory.user.MemoryUserStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MemoryStorage implements Storage {

    private final MemoryStorageSetting setting;
    private final UserStorage userStorage = new MemoryUserStorage();
    private final StockStorage stockStorage;

    public MemoryStorage(@NotNull StorageContext<MemoryStorageSetting> context) {
        this.setting = context.setting();
        this.stockStorage = context.setting().partialSaving() ? new MemoryPartialSavingStockStorage() : new MemoryStockStorage();
    }

    @Override
    public @NotNull String getName() {
        return "memory";
    }

    @Override
    public @NotNull List<Property> getInfo() {
        return Collections.emptyList();
    }

    @Override
    public void init() throws Exception {
        this.userStorage.init();
        this.stockStorage.init();
    }

    @Override
    public void close() {
    }

    @Override
    public @NotNull ItemStorage getItemStorage() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NotNull UserStorage getUserStorage() {
        return this.userStorage;
    }

    @Override
    public @NotNull StockStorage getStockStorage() {
        return this.stockStorage;
    }

    @Override
    public @NotNull CustomDataStorage getCustomDataStorage() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public @NotNull MemoryStorageSetting getSetting() {
        return this.setting;
    }
}
