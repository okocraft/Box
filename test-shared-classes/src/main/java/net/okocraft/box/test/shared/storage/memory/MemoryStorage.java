package net.okocraft.box.test.shared.storage.memory;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.CustomItemStorage;
import net.okocraft.box.storage.api.model.item.DefaultItemStorage;
import net.okocraft.box.storage.api.model.item.RemappedItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.model.version.StorageVersion;
import net.okocraft.box.storage.api.registry.StorageContext;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryPartialSavingStockStorage;
import net.okocraft.box.test.shared.storage.memory.stock.MemoryStockStorage;
import net.okocraft.box.test.shared.storage.memory.user.MemoryUserStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MemoryStorage implements Storage {

    private final MemoryStorageSetting setting;
    private final UserStorage userStorage = new MemoryUserStorage();
    private final StockStorage stockStorage;

    public MemoryStorage(@NotNull MemoryStorageSetting setting) {
        this.setting = setting;
        this.stockStorage = setting.partialSaving() ? new MemoryPartialSavingStockStorage() : new MemoryStockStorage();
    }

    public MemoryStorage(@NotNull StorageContext<MemoryStorageSetting> context) {
        this(context.setting());
    }

    @Override
    public @NotNull List<Property> getInfo() {
        return Collections.emptyList();
    }

    @Override
    public void init() {
    }

    @Override
    public boolean isFirstStartup() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
    }

    @Override
    public @NotNull UserStorage getUserStorage() {
        return this.userStorage;
    }

    @Override
    public @NotNull DefaultItemStorage defaultItemStorage() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NotNull CustomItemStorage customItemStorage() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NotNull RemappedItemStorage remappedItemStorage() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NotNull StockStorage getStockStorage() {
        return this.stockStorage;
    }

    @Override
    public @NotNull CustomDataStorage getCustomDataStorage() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @Nullable MCDataVersion getDataVersion() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void saveDataVersion(@NotNull MCDataVersion version) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NotNull StorageVersion getStorageVersion() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void saveStorageVersion(@NotNull StorageVersion version) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void applyStoragePatches(@NotNull StorageVersion current, @NotNull StorageVersion latest) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    public @NotNull MemoryStorageSetting getSetting() {
        return this.setting;
    }
}
