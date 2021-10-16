package net.okocraft.box.core.storage;

import net.okocraft.box.core.storage.model.data.CustomDataStorage;
import net.okocraft.box.core.storage.model.item.ItemStorage;
import net.okocraft.box.core.storage.model.stock.StockStorage;
import net.okocraft.box.core.storage.model.user.UserStorage;
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
