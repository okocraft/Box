package net.okocraft.box.core.storage.implementations.yaml;

import net.okocraft.box.api.util.Debugger;
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

    public YamlStorage(@NotNull Path rootDirectory) {
        this.rootDirectory = rootDirectory;
        this.userStorage = new YamlUserStorage(rootDirectory);
        this.itemStorage = new YamlItemStorage(rootDirectory);
        this.stockStorage = new YamlStockStorage(rootDirectory);
    }

    @Override
    public void init() throws Exception {
        Files.createDirectories(rootDirectory);

        Debugger.log(() -> "Initializing user storage...");
        userStorage.init();

        Debugger.log(() -> "Initializing item storage...");
        itemStorage.init();

        Debugger.log(() -> "Initializing stock storage...");
        stockStorage.init();
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
}
