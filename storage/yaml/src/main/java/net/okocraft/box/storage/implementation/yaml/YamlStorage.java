package net.okocraft.box.storage.implementation.yaml;

import com.github.siroshun09.configapi.core.serialization.annotation.DefaultString;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.registry.StorageContext;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class YamlStorage implements Storage {

    public static final String STORAGE_NAME = "Yaml";

    private final Path rootDirectory;
    private final YamlUserStorage userStorage;
    private final YamlItemStorage itemStorage;
    private final YamlStockStorage stockStorage;
    private final YamlCustomDataStorage customDataStorage;

    public YamlStorage(@NotNull StorageContext<Setting> context) {
        this.rootDirectory = context.pluginDirectory().resolve(context.setting().directoryName());
        this.userStorage = new YamlUserStorage(this.rootDirectory);
        this.itemStorage = new YamlItemStorage(this.rootDirectory);
        this.stockStorage = new YamlStockStorage(this.rootDirectory);
        this.customDataStorage = new YamlCustomDataStorage(this.rootDirectory);
    }

    @Override
    public @NotNull String getName() {
        return STORAGE_NAME;
    }

    @Override
    public @NotNull List<Property> getInfo() {
        return List.of(
                Property.of("directory-name", this.rootDirectory.getFileName().toString())
        );
    }

    @Override
    public void init() throws Exception {
        Files.createDirectories(this.rootDirectory);

        this.userStorage.init();
        this.itemStorage.init();
        this.stockStorage.init();
        this.customDataStorage.init();
    }

    @Override
    public void close() {
    }

    @Override
    public @NotNull ItemStorage getItemStorage() {
        return this.itemStorage;
    }

    @Override
    public @NotNull UserStorage getUserStorage() {
        return this.userStorage;
    }

    @Override
    public @NotNull StockStorage getStockStorage() {
        return this.stockStorage;
    }

    @Override
    public @NotNull CustomDataStorage getCustomDataStorage() {
        return this.customDataStorage;
    }

    public record Setting(@DefaultString("data") String directoryName) {
    }
}
