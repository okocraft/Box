package net.okocraft.box.storage.implementation.yaml;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.factory.item.BoxItemFactory;
import net.okocraft.box.storage.api.holder.LoggerHolder;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.storage.api.util.item.DefaultItemProvider;
import net.okocraft.box.storage.api.util.item.ItemNameGenerator;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

class YamlItemStorage implements ItemStorage {

    private final Path itemDirectory;
    private final YamlConfiguration itemStorageMeta;
    private final YamlConfiguration defaultItemData;
    private final YamlConfiguration customItemData;
    private final AtomicInteger lastUsedItemId = new AtomicInteger();

    private boolean migrationMode = false;

    YamlItemStorage(@NotNull Path rootDirectory) {
        this.itemDirectory = rootDirectory.resolve("items");
        this.itemStorageMeta = YamlConfiguration.create(itemDirectory.resolve("storage-meta.yml"));
        this.defaultItemData = YamlConfiguration.create(itemDirectory.resolve("default-items.yml"));
        this.customItemData = YamlConfiguration.create(itemDirectory.resolve("custom-items.yml"));
    }

    @Override
    public void init() throws Exception {
        Files.createDirectories(itemDirectory);

        itemStorageMeta.load();

        var oldIdFile = itemDirectory.resolve("last-used-item-id.dat");

        if (Files.exists(oldIdFile)) {
            migrationMode = true;

            var data = Files.readString(oldIdFile);
            Optional.ofNullable(parseIntOrNull(data)).ifPresent(lastUsedItemId::set);
            Files.delete(oldIdFile);
            saveLastUsedItemId();
        } else {
            itemStorageMeta.load();
            lastUsedItemId.set(itemStorageMeta.getInteger("last-used-item-id"));
        }
    }

    @Override
    public @Nullable MCDataVersion getDataVersion() {
        if (migrationMode) {
            return MCDataVersion.CURRENT; // to call #loadAllDefaultItems and #loadAllCustomItems
        } else if (itemStorageMeta.get("data-version") == null) {
            return null;
        } else {
            return MCDataVersion.of(itemStorageMeta.getInteger("data-version"));
        }
    }

    @Override
    public void saveCurrentDataVersion() throws Exception {
        itemStorageMeta.set("data-version", MCDataVersion.CURRENT.dataVersion());
        itemStorageMeta.save();
    }

    @Override
    public int getDefaultItemVersion() {
        return itemStorageMeta.getInteger("default-item-version");
    }

    @Override
    public void saveCurrentDefaultItemVersion() throws Exception {
        itemStorageMeta.set("default-item-version", DefaultItemProvider.version());
        itemStorageMeta.save();
    }

    @Override
    public @NotNull List<BoxItem> loadAllDefaultItems() throws Exception {
        if (migrationMode) {
            return migrateV4DefaultItems();
        }

        List<BoxItem> result;

        try (var source = defaultItemData.copy()) {
            source.load();
            var keyList = source.getKeyList();
            result = new ArrayList<>(keyList.size());

            for (var key : keyList) {
                var id = parseIntOrNull(key);
                var name = getPlainNameFromConfiguration(key, source);

                if (id == null) {
                    LoggerHolder.get().warning("Invalid id: " + key + " (name: " + name + ")");
                    continue;
                }

                var item = getItemStackFromConfiguration(key, source);
                result.add(BoxItemFactory.createDefaultItem(item, name, id));
            }
        }

        return result;
    }

    @Override
    public @NotNull List<BoxItem> updateDefaultItems(@NotNull Map<BoxItem, DefaultItem> itemMap) throws Exception {
        var result = new ArrayList<BoxItem>(itemMap.size());

        try (var target = defaultItemData.copy()) {
            target.load();

            for (var entry : itemMap.entrySet()) {
                var old = entry.getKey();
                var defaultItem = entry.getValue();

                saveDefaultItemToConfiguration(defaultItem, old.getInternalId(), target);

                result.add(BoxItemFactory.createDefaultItem(defaultItem, old.getInternalId()));
            }

            target.save();
        }

        return result;
    }

    @Override
    public @NotNull List<BoxItem> saveNewDefaultItems(@NotNull List<DefaultItem> newItems) throws Exception {
        var result = new ArrayList<BoxItem>(newItems.size());

        try (var target = defaultItemData.copy()) {
            target.load();

            for (var item : newItems) {
                int id = lastUsedItemId.incrementAndGet();

                saveDefaultItemToConfiguration(item, id, target);

                result.add(BoxItemFactory.createDefaultItem(item, id));
            }

            saveLastUsedItemId();
            target.save();
        }

        return result;
    }

    @Override
    public @NotNull List<BoxCustomItem> loadAllCustomItems() throws Exception {
        if (migrationMode) {
            return migrateV4CustomItems();
        }

        List<BoxCustomItem> result;

        try (var source = customItemData.copy()) {
            source.load();

            var keyList = source.getKeyList();
            result = new ArrayList<>(keyList.size());

            for (var key : keyList) {
                var id = parseIntOrNull(key);
                var name = getPlainNameFromConfiguration(key, source);

                if (id == null) {
                    LoggerHolder.get().warning("Invalid id: " + key + " (name: " + name + ")");
                    continue;
                }

                var item = getItemStackFromConfiguration(key, source);
                result.add(BoxItemFactory.createCustomItem(item, name, id));
            }
        }

        return result;
    }

    @Override
    public void updateCustomItems(@NotNull Collection<BoxCustomItem> items) throws Exception {
        try (var target = customItemData.copy()) {
            target.load();

            items.forEach(boxItem -> saveItemToConfiguration(boxItem, target));

            target.save();
        }
    }

    @Override
    public @NotNull BoxCustomItem saveNewCustomItem(@NotNull ItemStack item) throws Exception {
        return saveNewCustomItem(item, null);
    }

    @Override
    public @NotNull BoxCustomItem saveNewCustomItem(@NotNull ItemStack item, @Nullable String itemName) throws Exception {
        int id = lastUsedItemId.incrementAndGet();
        var plainName = itemName != null ? itemName : ItemNameGenerator.generate(item.getType().name(), item.serializeAsBytes());
        var boxItem = BoxItemFactory.createCustomItem(item, plainName, id);

        try (var target = customItemData.copy()) {
            target.load();

            saveItemToConfiguration(boxItem, target);

            saveLastUsedItemId();
            target.save();
        }

        return boxItem;
    }

    @Override
    public @NotNull BoxCustomItem rename(@NotNull BoxCustomItem item, @NotNull String newName) throws Exception {
        BoxItemFactory.renameCustomItem(item, newName);

        try (var target = customItemData.copy()) {
            target.load();

            saveItemToConfiguration(item, target);

            target.save();
        }

        return item;
    }

    private void saveLastUsedItemId() throws IOException {
        itemStorageMeta.set("last-used-item-id", lastUsedItemId.get());
        itemStorageMeta.save();
    }

    private @Nullable Integer parseIntOrNull(@NotNull String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private @NotNull String getPlainNameFromConfiguration(@NotNull String key, @NotNull Configuration source) {
        return source.getString(key + ".name");
    }

    private @NotNull ItemStack getItemStackFromConfiguration(@NotNull String key, @NotNull Configuration source) {
        return ItemStack.deserializeBytes(source.getBytes(key + ".data"));
    }

    private void saveItemToConfiguration(@NotNull BoxItem item, @NotNull Configuration target) {
        target.set(item.getInternalId() + ".name", item.getPlainName());
        target.setBytes(item.getInternalId() + ".data", item.getClonedItem().serializeAsBytes());
    }

    private void saveDefaultItemToConfiguration(@NotNull DefaultItem defaultItem,
                                                int id, @NotNull Configuration target) {
        target.set(id + ".name", defaultItem.plainName());
        target.setBytes(id + ".data", defaultItem.itemStack().serializeAsBytes());
    }

    private @NotNull List<BoxItem> migrateV4DefaultItems() throws Exception {
        var result = new ArrayList<BoxItem>();

        try (var source = defaultItemData.copy();
             var target = defaultItemData.copy()) {
            source.load();

            for (var defaultItem : DefaultItemProvider.all()) {
                int id = source.getInteger(defaultItem.plainName());

                if (id == 0) {
                    id = lastUsedItemId.incrementAndGet();
                }

                result.add(BoxItemFactory.createDefaultItem(defaultItem, id));
                saveDefaultItemToConfiguration(defaultItem, id, target);
            }

            target.save();
            saveLastUsedItemId();
        }

        return result;
    }

    private @NotNull List<BoxCustomItem> migrateV4CustomItems() throws Exception {
        var result = new ArrayList<BoxCustomItem>();

        try (var source = customItemData.copy();
             var target = customItemData.copy()) {
            source.load();

            for (var key : source.getKeyList()) {
                var id = parseIntOrNull(key);
                var name = getPlainNameFromConfiguration(key, source);

                if (id == null) {
                    LoggerHolder.get().warning("Invalid id: " + key + " (name: " + name + ")");
                    continue;
                }

                var item = getItemStackFromConfiguration(key, source);
                var boxCustomItem = BoxItemFactory.createCustomItem(item, name, id);

                result.add(boxCustomItem);
                saveItemToConfiguration(boxCustomItem, target);
            }

            target.save();
        }

        return result;
    }
}
