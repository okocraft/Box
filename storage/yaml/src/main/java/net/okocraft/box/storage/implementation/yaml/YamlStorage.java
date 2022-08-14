package net.okocraft.box.storage.implementation.yaml;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class YamlStorage implements Storage {

    public static final String STORAGE_NAME = "Yaml";

    private final Path rootDirectory;
    private final YamlUserStorage userStorage;
    private final YamlItemStorage itemStorage;
    private final YamlStockStorage stockStorage;
    private final YamlCustomDataStorage customDataStorage;

    public YamlStorage(@NotNull Path rootDirectory) {
        this.rootDirectory = rootDirectory;
        this.userStorage = new YamlUserStorage(rootDirectory);
        this.itemStorage = new YamlItemStorage(rootDirectory);
        this.stockStorage = new YamlStockStorage(rootDirectory);
        this.customDataStorage = new YamlCustomDataStorage(rootDirectory);
    }

    @Override
    public @NotNull String getName() {
        return STORAGE_NAME;
    }

    @Override
    public void init() throws Exception {
        Files.createDirectories(rootDirectory);

        userStorage.init();
        itemStorage.init();
        stockStorage.init();
        customDataStorage.init();
    }

    @Override
    public void close() {
    }

    @Override
    public @NotNull ItemStorage getItemStorage() {
        return itemStorage;
    }

    @Override
    public @NotNull UserStorage getUserStorage() {
        return userStorage;
    }

    @Override
    public @NotNull StockStorage getStockStorage() {
        return stockStorage;
    }

    @Override
    public @NotNull CustomDataStorage getCustomDataStorage() {
        return customDataStorage;
    }
}
