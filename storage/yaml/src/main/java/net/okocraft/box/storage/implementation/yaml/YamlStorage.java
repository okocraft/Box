package net.okocraft.box.storage.implementation.yaml;

import dev.siroshun.configapi.core.serialization.annotation.DefaultString;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.CustomItemStorage;
import net.okocraft.box.storage.api.model.item.DefaultItemStorage;
import net.okocraft.box.storage.api.model.item.RemappedItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.model.version.StorageVersion;
import net.okocraft.box.storage.api.registry.StorageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class YamlStorage implements Storage {

    public static final String STORAGE_NAME = "Yaml";

    private final Path rootDirectory;
    final YamlMetaStorage metaStorage;
    final YamlUserStorage userStorage;
    final YamlDefaultItemStorage defaultItemStorage;
    final YamlCustomItemStorage customItemStorage;
    final YamlRemappedItemStorage remappedItemStorage;
    final YamlStockStorage stockStorage;
    final YamlCustomDataStorage customDataStorage;

    public YamlStorage(@NotNull StorageContext<Setting> context) {
        this.rootDirectory = context.pluginDirectory().resolve(context.setting().directoryName());
        this.metaStorage = new YamlMetaStorage(this.rootDirectory);
        this.userStorage = new YamlUserStorage(this.rootDirectory);
        this.defaultItemStorage = new YamlDefaultItemStorage(this.rootDirectory, this.metaStorage);
        this.customItemStorage = new YamlCustomItemStorage(this.rootDirectory, this.metaStorage);
        this.remappedItemStorage = new YamlRemappedItemStorage(this.rootDirectory);
        this.stockStorage = new YamlStockStorage(this.rootDirectory);
        this.customDataStorage = new YamlCustomDataStorage(this.rootDirectory);
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

        var oldMetaFilepath = this.rootDirectory.resolve("items").resolve("storage-meta.yml");
        if (Files.isRegularFile(oldMetaFilepath)) {
            this.metaStorage.migrateFromOldMetaFile(oldMetaFilepath);
        }

        this.metaStorage.load();
    }

    @Override
    public void prepare() throws Exception {
        this.userStorage.init();
        this.stockStorage.init();
        this.customDataStorage.init();
    }

    @Override
    public void close() {
    }

    @Override
    public @NotNull DefaultItemStorage defaultItemStorage() {
        return this.defaultItemStorage;
    }

    @Override
    public @NotNull CustomItemStorage customItemStorage() {
        return this.customItemStorage;
    }

    @Override
    public @NotNull RemappedItemStorage remappedItemStorage() {
        return this.remappedItemStorage;
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

    @Override
    public @Nullable MCDataVersion getDataVersion() {
        return this.metaStorage.dataVersion();
    }

    @Override
    public void saveDataVersion(@NotNull MCDataVersion version) throws Exception {
        this.metaStorage.dataVersion(version);
    }

    @Override
    public @NotNull StorageVersion getStorageVersion() {
        return this.metaStorage.storageVersion();
    }

    @Override
    public void saveStorageVersion(@NotNull StorageVersion version) throws Exception {
        this.metaStorage.storageVersion(version);
    }

    @Override
    public void applyStoragePatches(@NotNull StorageVersion current, @NotNull StorageVersion latest) throws Exception {
        if (current.isBefore(StorageVersion.V6) && latest.isAfterOrSame(StorageVersion.V6)) {
            YamlStoragePatches.v6(this);
        }
    }

    public record Setting(@DefaultString("data") String directoryName) {
    }
}
