package net.okocraft.box.test.shared.storage.dummy;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.registry.StorageContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class DummyStorage implements Storage {

    @SuppressWarnings("unused")
    public DummyStorage(@NotNull StorageContext<DummyStorageSetting> context) {
    }

    @Override
    public @NotNull String getName() {
        return "dummy";
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
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ItemStorage getItemStorage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull UserStorage getUserStorage() {
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
}
