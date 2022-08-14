package net.okocraft.box.core.model.loader;

import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.core.model.manager.BoxItemManager;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.storage.api.util.item.DefaultItemProvider;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ItemLoader {

    public static @NotNull BoxItemManager load(@NotNull ItemStorage itemStorage) throws Exception {
        var defaultItems = loadDefaultItems(itemStorage);
        var customItems = loadCustomItems(itemStorage);

        var itemManger = new BoxItemManager(itemStorage);

        itemManger.storeItems(defaultItems);
        itemManger.storeItems(customItems);

        itemStorage.saveCurrentDataVersion();

        return itemManger;
    }

    private static @NotNull List<BoxItem> loadDefaultItems(@NotNull ItemStorage storage) throws Exception {
        var version = storage.getDataVersion();

        if (version == 0) {
            return storage.saveNewDefaultItems(DefaultItemProvider.all());
        } else if (version == currentDataVersion()) {
            return storage.loadAllDefaultItems();
        } else {
            return updateDefaultItems(storage);
        }
    }

    private static @NotNull List<BoxItem> updateDefaultItems(@NotNull ItemStorage storage) throws Exception {
        var oldItemMap = storage.loadAllDefaultItems().stream().collect(Collectors.toMap(BoxItem::getOriginal, Function.identity()));
        var oldToNewItemMap = new HashMap<BoxItem, DefaultItem>();
        var newItems = new ArrayList<DefaultItem>();

        for (var item : DefaultItemProvider.all()) {
            var oldItem = oldItemMap.get(item.itemStack());
            if (oldItem != null) {
                oldToNewItemMap.put(oldItem, item);
            } else {
                newItems.add(item);
            }
        }

        var defaultItems = new ArrayList<BoxItem>();

        defaultItems.addAll(storage.updateDefaultItems(oldToNewItemMap));
        defaultItems.addAll(storage.saveNewDefaultItems(newItems));

        return defaultItems;
    }

    private static @NotNull List<BoxCustomItem> loadCustomItems(@NotNull ItemStorage storage) throws Exception {
        var version = storage.getDataVersion();

        if (version == 0) {
            return Collections.emptyList();
        } else if (version == currentDataVersion()) {
            return storage.loadAllCustomItems();
        } else {
            var items = storage.loadAllCustomItems();
            storage.updateCustomItems(items);
            return items;
        }
    }

    @SuppressWarnings("deprecation")
    private static int currentDataVersion() {
        return Bukkit.getUnsafe().getDataVersion();
    }
}
