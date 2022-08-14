package net.okocraft.box.storage.api.model;

import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractStorage implements Storage {

    private final String name;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;
    private final StockStorage stockStorage;
    private final CustomDataStorage customDataStorage;

    protected AbstractStorage(@NotNull String name,
                              @NotNull UserStorage userStorage, @NotNull ItemStorage itemStorage,
                              @NotNull StockStorage stockStorage, @NotNull CustomDataStorage customDataStorage) {
        this.name = name;
        this.userStorage = Objects.requireNonNull(userStorage);
        this.itemStorage = Objects.requireNonNull(itemStorage);
        this.stockStorage = Objects.requireNonNull(stockStorage);
        this.customDataStorage = Objects.requireNonNull(customDataStorage);
    }

    @Override
    public final void init() throws Exception {
        initStorage();

        userStorage.init();
        itemStorage.init();
        stockStorage.init();
        customDataStorage.init();
    }

    @Override
    public final void close() throws Exception {
        customDataStorage.close();
        stockStorage.close();
        itemStorage.close();
        userStorage.close();

        closeStorage();
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull UserStorage getUserStorage() {
        return userStorage;
    }

    @Override
    public @NotNull ItemStorage getItemStorage() {
        return itemStorage;
    }

    @Override
    public @NotNull StockStorage getStockStorage() {
        return stockStorage;
    }

    @Override
    public @NotNull CustomDataStorage getCustomDataStorage() {
        return customDataStorage;
    }

    protected abstract void initStorage() throws Exception;

    protected abstract void closeStorage() throws Exception;
}
