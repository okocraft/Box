package net.okocraft.box.storage.migrator.data;

import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.util.item.BoxItemSupplier;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.storage.api.util.item.DefaultItemProvider;
import net.okocraft.box.storage.migrator.StorageMigrator;
import net.okocraft.box.storage.migrator.util.LoggerWrapper;
import net.okocraft.box.storage.migrator.util.MigratedBoxItemSupplier;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ItemMigrator implements DataMigrator<ItemStorage> {

    @Override
    public @NotNull ItemStorage getDataStorage(@NotNull Storage storage) {
        return storage.getItemStorage();
    }

    @Override
    public void migrate(@NotNull ItemStorage source, @NotNull ItemStorage target, @NotNull LoggerWrapper logger) throws Exception {
        var oldIdToItemNameMap = new HashMap<Integer, String>(300, 0.95f);
        var newItemNameToItemMap = new HashMap<String, BoxItem>(300, 0.95f);

        var targetDataVersion = target.getDataVersion();

        logger.info("Updating default items in the target storage...");

        for (var defaultItem : loadDefaultItems(target, targetDataVersion)) {
            newItemNameToItemMap.put(defaultItem.getPlainName(), defaultItem);
        }

        logger.info("Loading default items from the source storage...");

        for (var defaultItem : source.loadAllDefaultItems()) {
            oldIdToItemNameMap.put(defaultItem.getInternalId(), defaultItem.getPlainName());

            if (!newItemNameToItemMap.containsKey(defaultItem.getPlainName())) {
                logger.warning(defaultItem.getPlainName() + " is not found in current default items. (Renamed or removed in newer version?)");
            }
        }

        logger.info("Updating custom items in the target storage...");

        for (var customItem : loadCustomItems(target, targetDataVersion)) {
            newItemNameToItemMap.put(customItem.getPlainName(), customItem);
        }

        logger.info("Loading custom items from the source storage...");

        for (var customItem : source.loadAllCustomItems()) {
            oldIdToItemNameMap.put(customItem.getInternalId(), customItem.getPlainName());

            if (!newItemNameToItemMap.containsKey(customItem.getPlainName())) {
                var newCustomItem = target.saveNewCustomItem(customItem.getOriginal(), customItem.getPlainName());
                newItemNameToItemMap.put(newCustomItem.getPlainName(), newCustomItem);
                logger.info(customItem.getPlainName() + " has been registered to new storage.");
            }
        }

        target.saveCurrentDataVersion();

        if (StorageMigrator.debug) {
            boolean first = false;
            for (var oldEntry : oldIdToItemNameMap.entrySet()) {
                if (!first) {
                    logger.info("--- Item Id Map ---");
                    first = true;
                }

                var newItem = newItemNameToItemMap.get(oldEntry.getValue());
                if (oldEntry.getKey() != newItem.getInternalId()) {
                    logger.info(newItem.getPlainName() + ": " + oldEntry.getKey() + " -> " + newItem.getInternalId());
                }
            }
        }

        logger.info(oldIdToItemNameMap.size() + " items are migrated.");
        BoxItemSupplier.ITEM_FUNCTION = new MigratedBoxItemSupplier(oldIdToItemNameMap, newItemNameToItemMap);
    }

    // FIXME: this code was copied from ItemLoader
    private static @NotNull List<BoxItem> loadDefaultItems(@NotNull ItemStorage storage, int dataVersion) throws Exception {
        if (dataVersion == 0) {
            return storage.saveNewDefaultItems(DefaultItemProvider.all());
        } else if (dataVersion == currentDataVersion()) {
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

        var defaultItems = new ArrayList<BoxItem>(oldToNewItemMap.size() + newItems.size());

        defaultItems.addAll(storage.updateDefaultItems(oldToNewItemMap));
        defaultItems.addAll(storage.saveNewDefaultItems(newItems));

        return defaultItems;
    }

    private static @NotNull List<BoxCustomItem> loadCustomItems(@NotNull ItemStorage storage, int dataVersion) throws Exception {
        if (dataVersion == 0) {
            return Collections.emptyList();
        } else if (dataVersion == currentDataVersion()) {
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
