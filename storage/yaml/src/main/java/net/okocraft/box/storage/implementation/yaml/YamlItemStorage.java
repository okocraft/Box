package net.okocraft.box.storage.implementation.yaml;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.core.node.NumberValue;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxDefaultItem;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.factory.item.BoxItemFactory;
import net.okocraft.box.storage.api.model.item.ItemData;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.storage.api.util.item.ItemVersion;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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

        var metaData = YamlFormat.DEFAULT.load(this.itemStorageMetaFile);

        var dataVersion = metaData.get("data-version");

        if (dataVersion instanceof NumberValue numberValue) {
            this.dataVersion = MCDataVersion.of(numberValue.asInt());
            this.defaultItemVersion = metaData.getInteger("default-item-version");
        }

        this.lastUsedItemId.set(metaData.getInteger("last-used-item-id"));
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
    public <I> @NotNull List<I> loadAllDefaultItems(@NotNull Function<ItemData, I> function) throws Exception {
        var source = YamlFormat.DEFAULT.load(this.defaultItemDataFile);
        var keys = source.value().keySet();
        var result = new ArrayList<I>(keys.size());

        for (var key : source.value().keySet()) {
            var data = readItemData(source, key);

            if (data != null) {
                result.add(function.apply(data));
            }
        }

        return result;
    }

    @Override
    public @NotNull List<BoxDefaultItem> saveDefaultItems(@NotNull List<DefaultItem> newItems, @NotNull Int2ObjectMap<DefaultItem> updatedItemMap) throws Exception {
        var result = new ArrayList<BoxDefaultItem>(newItems.size() + updatedItemMap.size());

        try (var writer = Files.newBufferedWriter(this.defaultItemDataFile, StandardCharsets.UTF_8, YamlFileOptions.WRITE)) {
            for (var item : newItems) {
                int id = this.lastUsedItemId.incrementAndGet();

                writeItemData(id, item.plainName(), item.itemStack().serializeAsBytes(), writer);
                result.add(BoxItemFactory.createDefaultItem(id, item));
            }

            for (var entry : updatedItemMap.int2ObjectEntrySet()) {
                var internalId = entry.getIntKey();
                var item = entry.getValue();

                writeItemData(internalId, item.plainName(), item.itemStack().serializeAsBytes(), writer);
                result.add(BoxItemFactory.createDefaultItem(internalId, item));
            }
        }

        this.saveItemStorageMeta();

        return result;
    }

    @Override
    public <I> @NotNull List<I> loadAllCustomItems(@NotNull Function<ItemData, I> function) throws Exception {
        var source = YamlFormat.DEFAULT.load(this.customItemDataFile);
        var keys = source.value().keySet();
        var result = new ArrayList<I>(keys.size());

        for (var key : keys) {
            var data = readItemData(source, key);

            if (data != null) {
                result.add(function.apply(data));
            }
        }

        return result;
    }

    @Override
    public void updateCustomItems(@NotNull Collection<BoxCustomItem> items) throws Exception {
        try (var writer = Files.newBufferedWriter(this.customItemDataFile, StandardCharsets.UTF_8, YamlFileOptions.WRITE)) {
            for (var item : items) {
                writeItemData(item.getInternalId(), item.getPlainName(), item.getOriginal().serializeAsBytes(), writer);
            }
        }
    }

    @Override
    public @NotNull BoxCustomItem saveNewCustomItem(@NotNull ItemStack item, @Nullable String itemName) throws Exception {
        int id = this.lastUsedItemId.incrementAndGet();
        var name = itemName != null ? itemName : ItemNameGenerator.itemStack(item.getType(), item.serializeAsBytes());
        var boxItem = BoxItemFactory.createCustomItem(id, name, item);

        try (var writer = Files.newBufferedWriter(this.customItemDataFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.WRITE)) {
            writeItemData(id, boxItem.getPlainName(), boxItem.getOriginal().serializeAsBytes(), writer);
        }

        this.saveItemStorageMeta();

        return boxItem;
    }

    @Override
    public @NotNull BoxCustomItem renameCustomItem(@NotNull BoxCustomItem item, @NotNull String newName) throws Exception {
        BoxItemFactory.renameCustomItem(item, newName);

        var mapNode = YamlFormat.DEFAULT.load(this.customItemDataFile);

        var data = mapNode.getOrCreateMap(item.getInternalId());
        data.set("name", item.getPlainName());
        data.set("data", Base64.getEncoder().encodeToString(item.getOriginal().serializeAsBytes()));

        YamlFormat.DEFAULT.save(mapNode, this.customItemDataFile);

        return item;
    }

    private static @Nullable ItemData readItemData(@NotNull MapNode source, @NotNull Object key) {
        int id;

        var data = source.getMap(key);
        var name = data.getString("name");

        try {
            id = key instanceof Number num ? num.intValue() : Integer.parseInt(String.valueOf(key));
        } catch (NumberFormatException e) {
            BoxLogger.logger().warn("Invalid id: {} (name: {})", key, name);
            return null;
        }

        return new ItemData(id, name, Base64.getDecoder().decode(data.getString("data")));
    }

    private static void writeItemData(int id, @NotNull String plainName, byte @NotNull [] itemData, @NotNull BufferedWriter writer) throws IOException {
        writer.write(Integer.toString(id));
        writer.write(":");
        writer.newLine();
        writer.write("  name: ");
        writer.write(plainName);
        writer.newLine();
        writer.write("  data: ");
        writer.write(Base64.getEncoder().encodeToString(itemData));
        writer.newLine();
    }

    private void saveItemStorageMeta() throws IOException {
        try (var writer = Files.newBufferedWriter(this.itemStorageMetaFile, StandardCharsets.UTF_8, YamlFileOptions.WRITE)) {
            if (this.dataVersion != null) {
                writer.write("data-version: ");
                writer.write(Integer.toString(this.dataVersion.dataVersion()));
                writer.newLine();
            }

            if (this.defaultItemVersion != 0) {
                writer.write("default-item-version: ");
                writer.write(Integer.toString(this.defaultItemVersion));
                writer.newLine();
            }

            writer.write("last-used-item-id: ");
            writer.write(Integer.toString(this.lastUsedItemId.get()));
            writer.newLine();
        }
    }
}
