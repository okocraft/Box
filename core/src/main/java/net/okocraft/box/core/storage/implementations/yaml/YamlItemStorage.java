package net.okocraft.box.core.storage.implementations.yaml;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.core.model.item.BoxCustomItemImpl;
import net.okocraft.box.core.storage.model.item.AbstractItemStorage;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class YamlItemStorage extends AbstractItemStorage {

    private final Path itemDirectory;
    private final Path lastUsedItemIdFile;
    private final YamlConfiguration defaultItemData;
    private final YamlConfiguration customItemData;
    private final AtomicInteger lastUsedItemId = new AtomicInteger();

    YamlItemStorage(@NotNull Path rootDirectory) {
        this.itemDirectory = rootDirectory.resolve("items");
        this.lastUsedItemIdFile = itemDirectory.resolve("last-used-item-id.dat");
        this.defaultItemData = YamlConfiguration.create(itemDirectory.resolve("default-items.yml"));
        this.customItemData = YamlConfiguration.create(itemDirectory.resolve("custom-items.yml"));
    }

    @Override
    public void init() throws Exception {
        Files.createDirectories(itemDirectory);

        if (Files.exists(lastUsedItemIdFile)) {
            var data = Files.readString(lastUsedItemIdFile);
            lastUsedItemId.set(Integer.parseInt(data));
        }
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isRegisteredItem(@NotNull ItemStack itemStack) throws Exception {
        try (var dumped = YamlConfiguration.create(itemDirectory.resolve(Bukkit.getMinecraftVersion() + ".yml"))) {
            dumped.load();

            for (var key : dumped.getKeyList()) {
                var other = ItemStack.deserializeBytes(dumped.getBytes(key + ".data"));

                if (itemStack.isSimilar(other)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isUsedName(@NotNull String name) throws Exception {
        try (var dumped = YamlConfiguration.create(itemDirectory.resolve(Bukkit.getMinecraftVersion() + ".yml"))) {
            dumped.load();

            for (var key : dumped.getKeyList()) {
                var other = dumped.getString(key + ".name");

                if (name.equals(other)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void saveCustomItem(@NotNull BoxCustomItemImpl customItem) throws Exception {
        saveNewCustomItem(customItem);
        saveVersionedItem(Bukkit.getMinecraftVersion(), customItem);
    }

    @Override
    protected int getNewItemId() {
        return lastUsedItemId.incrementAndGet();
    }

    @Override
    protected int getDefaultItemId(@NotNull String name) {
        int id = defaultItemData.getInteger(name, -1);

        if (id == -1) {
            id = getNewItemId();
            defaultItemData.set(name, id);
        }

        return id;
    }

    @Override
    protected void saveVersionedItem(@NotNull String version, @NotNull BoxItem item) throws Exception {
        var file = YamlConfiguration.create(itemDirectory.resolve(version + ".yml"));

        if (Files.exists(file.getPath())) {
            file.load();
        }

        file.set(item.getInternalId() + ".name", item.getPlainName());
        file.setBytes(item.getInternalId() + ".data", item.getOriginal().serializeAsBytes());

        file.save();
    }

    @Override
    protected void saveVersionedItems(@NotNull String version, @NotNull Collection<BoxItem> items) throws Exception {
        var file = YamlConfiguration.create(itemDirectory.resolve(version + ".yml"));

        for (var item : items) {
            file.set(item.getInternalId() + ".name", item.getPlainName());
            file.setBytes(item.getInternalId() + ".data", item.getOriginal().serializeAsBytes());
        }

        file.save();
    }

    @Override
    protected @NotNull List<BoxCustomItem> loadCustomItems() {
        var keys = customItemData.getKeyList();
        var result = new ArrayList<BoxCustomItem>(keys.size());

        for (var key : keys) {
            int id;

            try {
                id = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                BoxProvider.get().getLogger().warning("Could not parse an id to a number (" + key + ")");
                continue;
            }

            String name = customItemData.getString(key + ".name");

            if (name.isEmpty()) {
                BoxProvider.get().getLogger().warning("Could not get an item name (" + key + ")");
                continue;
            }

            ItemStack item;

            try {
                item = ItemStack.deserializeBytes(customItemData.getBytes(key + ".data"));
            } catch (IllegalArgumentException e) {
                BoxProvider.get().getLogger()
                        .warning("Could not load a custom item because it may be invalid Base64 schema. (" + key + ")");
                continue;
            }

            result.add(new BoxCustomItemImpl(item, name, id));
        }

        return result;
    }

    @Override
    protected void updateCustomItems(@NotNull List<BoxCustomItem> customItems) {
        for (var item : customItems) {
            customItemData.setBytes(item.getInternalId() + ".data", item.getOriginal().serializeAsBytes());
        }
    }

    @Override
    protected void saveNewCustomItem(@NotNull BoxCustomItem item) throws Exception {
        try (var yaml = customItemData.copy()) {
            yaml.load();

            var key = String.valueOf(item.getInternalId());

            yaml.set(key + ".name", item.getPlainName());
            yaml.setBytes(key + ".data", item.getOriginal().serializeAsBytes());

            yaml.save();
        }
    }

    @Override
    protected void onLoadingAllItemsStarted() throws Exception {
        defaultItemData.load();
        customItemData.load();
    }

    @Override
    protected void onLoadingAllItemsFinished() throws Exception {
        Files.writeString(
                lastUsedItemIdFile, String.valueOf(lastUsedItemId.get()),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE
        );

        defaultItemData.save();
        customItemData.save();
    }
}
