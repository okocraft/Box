package net.okocraft.box.test.shared.storage.dummy;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DummyStorage implements Storage {

    @SuppressWarnings("unused")
    public DummyStorage(@NotNull StorageContext<DummyStorageSetting> context) {
    }

    @Override
    public @NotNull List<Property> getInfo() {
        return Collections.emptyList();
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull UserStorage getUserStorage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull DefaultItemStorage defaultItemStorage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull CustomItemStorage customItemStorage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull RemappedItemStorage remappedItemStorage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull StockStorage getStockStorage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull CustomDataStorage getCustomDataStorage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable MCDataVersion getDataVersion() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveDataVersion(@NotNull MCDataVersion version) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull StorageVersion getStorageVersion() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyStoragePatches(@NotNull StorageVersion current, @NotNull StorageVersion latest) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveStorageVersion(@NotNull StorageVersion version) throws Exception {
        throw new UnsupportedOperationException();
    }
}
