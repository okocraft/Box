package net.okocraft.box.core.storage.implementations.yaml;

import net.okocraft.box.core.storage.Storage;
import net.okocraft.box.core.storage.model.item.ItemStorage;
import net.okocraft.box.core.storage.model.stock.StockStorage;
import net.okocraft.box.core.storage.model.user.UserStorage;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class YamlStorage implements Storage {

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
    public @NotNull YamlCustomDataStorage getCustomDataStorage() {
        return customDataStorage;
    }
}
