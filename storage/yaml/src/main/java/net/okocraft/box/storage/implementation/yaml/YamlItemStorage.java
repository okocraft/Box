package net.okocraft.box.storage.implementation.yaml;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.factory.item.BoxItemFactory;
import net.okocraft.box.storage.api.holder.LoggerHolder;
import net.okocraft.box.storage.api.model.item.ItemData;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.storage.api.util.item.ItemVersion;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

class YamlItemStorage implements ItemStorage {

    private final Path itemDirectory;
    private final Path itemStorageMetaFile;
    private final Path defaultItemDataFile;
    private final Path customItemDataFile;
    private final AtomicInteger lastUsedItemId = new AtomicInteger();

    private MCDataVersion dataVersion;
    private int defaultItemVersion;

    YamlItemStorage(@NotNull Path rootDirectory) {
        this.itemDirectory = rootDirectory.resolve("items");
        this.itemStorageMetaFile = this.itemDirectory.resolve("storage-meta.yml");
        this.defaultItemDataFile = this.itemDirectory.resolve("default-items.yml");
        this.customItemDataFile = this.itemDirectory.resolve("custom-items.yml");
    }

    @Override
    public void init() throws Exception {
        Files.createDirectories(this.itemDirectory);

        try (var itemStorageMetaYaml = YamlConfiguration.create(this.itemStorageMetaFile)) {
            itemStorageMetaYaml.load();

            if (itemStorageMetaYaml.get("data-version") != null) {
                this.dataVersion = MCDataVersion.of(itemStorageMetaYaml.getInteger("data-version"));
                this.defaultItemVersion = itemStorageMetaYaml.getInteger("default-item-version");
            }

            this.lastUsedItemId.set(itemStorageMetaYaml.getInteger("last-used-item-id"));
        }
    }

    @Override
    public @NotNull Optional<ItemVersion> getItemVersion() {
        return this.dataVersion != null ? Optional.of(new ItemVersion(this.dataVersion, this.defaultItemVersion)) : Optional.empty();
    }

    @Override
    public void saveItemVersion(@NotNull ItemVersion itemVersion) throws Exception {
        this.dataVersion = itemVersion.dataVersion();
        this.defaultItemVersion = itemVersion.defaultItemProviderVersion();

        this.saveItemStorageMeta();
    }

    @Override
    public @NotNull List<ItemData> loadAllDefaultItems() throws Exception {
        List<ItemData> result;

        try (var source = YamlConfiguration.create(this.defaultItemDataFile)) {
            source.load();

            var keyList = source.getKeyList();
            result = new ArrayList<>(keyList.size());

            for (var key : keyList) {
                var id = this.parseIntOrNull(key);
                var name = this.readPlainName(source, key);

                if (id == null) {
                    LoggerHolder.get().warning("Invalid id: " + key + " (name: " + name + ")");
                    continue;
                }

                result.add(new ItemData(id, name, source.getBytes(key + ".data")));
            }
        }

        return result;
    }

    @Override
    public @NotNull List<BoxItem> saveDefaultItems(@NotNull List<DefaultItem> newItems, @NotNull Int2ObjectMap<DefaultItem> updatedItemMap) throws Exception {
        var result = new ArrayList<BoxItem>(newItems.size() + updatedItemMap.size());

        try (var target = YamlConfiguration.create(this.defaultItemDataFile)) {
            for (var item : newItems) {
                int id = this.lastUsedItemId.incrementAndGet();

                this.writeDefaultItem(id, item, target);

                result.add(BoxItemFactory.createDefaultItem(id, item));
            }

            for (var entry : updatedItemMap.int2ObjectEntrySet()) {
                var internalId = entry.getIntKey();
                var item = entry.getValue();

                this.writeDefaultItem(internalId, item, target);

                result.add(BoxItemFactory.createDefaultItem(internalId, item));
            }

            target.save();
        }

        this.saveItemStorageMeta();

        return result;
    }

    @Override
    public @NotNull List<BoxCustomItem> loadAllCustomItems() throws Exception {
        List<BoxCustomItem> result;

        try (var source = YamlConfiguration.create(this.customItemDataFile)) {
            source.load();

            var keyList = source.getKeyList();
            result = new ArrayList<>(keyList.size());

            for (var key : keyList) {
                var id = this.parseIntOrNull(key);
                var name = this.readPlainName(source, key);

                if (id == null) {
                    LoggerHolder.get().warning("Invalid id: " + key + " (name: " + name + ")");
                    continue;
                }

                var item = ItemStack.deserializeBytes(source.getBytes(key + ".data"));
                result.add(BoxItemFactory.createCustomItem(id, name, item));
            }
        }

        return result;
    }

    @Override
    public void updateCustomItems(@NotNull Collection<BoxCustomItem> items) throws Exception {
        try (var target = YamlConfiguration.create(this.customItemDataFile)) {
            items.forEach(boxItem -> this.writeCustomItem(boxItem, target));
            target.save();
        }
    }

    @Override
    public @NotNull BoxCustomItem saveNewCustomItem(@NotNull ItemStack item, @Nullable String itemName) throws Exception {
        int id = this.lastUsedItemId.incrementAndGet();
        var name = itemName != null ? itemName : ItemNameGenerator.itemStack(item.getType(), item.serializeAsBytes());
        var boxItem = BoxItemFactory.createCustomItem(id, name, item);

        try (var target = YamlConfiguration.create(this.customItemDataFile)) {
            target.load();
            this.writeCustomItem(boxItem, target);
            target.save();
        }

        this.saveItemStorageMeta();

        return boxItem;
    }

    @Override
    public @NotNull BoxCustomItem renameCustomItem(@NotNull BoxCustomItem item, @NotNull String newName) throws Exception {
        try (var target = YamlConfiguration.create(this.customItemDataFile)) {
            target.load();
            this.writeCustomItem(item, target);
            target.save();
        }

        BoxItemFactory.renameCustomItem(item, newName);

        return item;
    }

    private @Nullable Integer parseIntOrNull(@NotNull String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private @NotNull String readPlainName(@NotNull Configuration source, @NotNull String key) {
        return source.getString(key + ".name");
    }

    private void writeDefaultItem(int id, @NotNull DefaultItem defaultItem, @NotNull Configuration target) {
        target.set(id + ".name", defaultItem.plainName());
        target.setBytes(id + ".data", defaultItem.itemStack().serializeAsBytes());
    }

    private void writeCustomItem(@NotNull BoxCustomItem item, @NotNull Configuration target) {
        target.set(item.getInternalId() + ".name", item.getPlainName());
        target.setBytes(item.getInternalId() + ".data", item.getClonedItem().serializeAsBytes());
    }

    private void saveItemStorageMeta() throws IOException {
        try (var writer = Files.newBufferedWriter(this.itemStorageMetaFile, StandardCharsets.UTF_8, YamlFileOptions.WRITE)) {
            if (this.dataVersion != null) {
                writer.write("data-version: ");
                writer.write(this.dataVersion.dataVersion());
                writer.newLine();
            }

            if (this.defaultItemVersion != 0) {
                writer.write("default-item-version: ");
                writer.write(this.defaultItemVersion);
                writer.newLine();
            }

            writer.write("last-used-item-id: ");
            writer.write(this.lastUsedItemId.get());
        }
    }
}
