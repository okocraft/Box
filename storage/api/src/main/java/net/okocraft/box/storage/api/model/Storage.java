package net.okocraft.box.storage.api.model;

import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Storage {

    @NotNull String getName();

    @NotNull List<Property> getInfo();

    void init() throws Exception;

    void close() throws Exception;

    @NotNull ItemStorage getItemStorage();

    @NotNull UserStorage getUserStorage();

    @NotNull StockStorage getStockStorage();

    @NotNull CustomDataStorage getCustomDataStorage();

    record Property(@NotNull String key, @NotNull String value) {

        public static @NotNull Property of(@NotNull String key, @NotNull String value) {
            return new Property(key, value);
        }

        public @NotNull String asString() {
            return this.key + ": " + this.value;
        }
    }
}
