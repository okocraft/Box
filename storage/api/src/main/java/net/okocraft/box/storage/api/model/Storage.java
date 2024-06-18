package net.okocraft.box.storage.api.model;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.CustomItemStorage;
import net.okocraft.box.storage.api.model.item.DefaultItemStorage;
import net.okocraft.box.storage.api.model.item.RemappedItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.model.version.StorageVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Storage {

    @NotNull
    List<Property> getInfo();

    void init() throws Exception;

    void prepare() throws Exception;

    void close() throws Exception;

    @NotNull
    DefaultItemStorage defaultItemStorage();

    @NotNull
    CustomItemStorage customItemStorage();

    @NotNull
    RemappedItemStorage remappedItemStorage();

    @NotNull
    UserStorage getUserStorage();

    @NotNull
    StockStorage getStockStorage();

    @NotNull
    CustomDataStorage getCustomDataStorage();

    @Nullable
    MCDataVersion getDataVersion() throws Exception;

    void saveDataVersion(@NotNull MCDataVersion version) throws Exception;

    @NotNull
    StorageVersion getStorageVersion() throws Exception;

    void saveStorageVersion(@NotNull StorageVersion version) throws Exception;

    void applyStoragePatches(@NotNull StorageVersion current, @NotNull StorageVersion latest) throws Exception;

    record Property(@NotNull String key, @NotNull String value) {

        public static @NotNull Property of(@NotNull String key, @NotNull String value) {
            return new Property(key, value);
        }

        public @NotNull String asString() {
            return this.key + ": " + this.value;
        }
    }
}
