package net.okocraft.box.storage.api.model;

import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import org.jetbrains.annotations.NotNull;

public interface Storage {

    @NotNull String getName();

    void init() throws Exception;

    void close() throws Exception;

    @NotNull ItemStorage getItemStorage();

    @NotNull UserStorage getUserStorage();

    @NotNull StockStorage getStockStorage();

    @NotNull CustomDataStorage getCustomDataStorage();
}
