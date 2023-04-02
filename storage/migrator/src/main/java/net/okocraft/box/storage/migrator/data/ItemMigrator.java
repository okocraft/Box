package net.okocraft.box.storage.migrator.data;

import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.util.item.BoxItemSupplier;
import net.okocraft.box.storage.api.util.item.DefaultItemProvider;
import net.okocraft.box.storage.api.util.item.DefaultItemUpdater;
import net.okocraft.box.storage.migrator.StorageMigrator;
import net.okocraft.box.storage.migrator.util.LoggerWrapper;
import net.okocraft.box.storage.migrator.util.MigratedBoxItemSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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

        for (var defaultItem : loadDefaultItems(target, targetDataVersion, target.getDefaultItemVersion())) {
            newItemNameToItemMap.put(defaultItem.getPlainName(), defaultItem);
        }

        logger.info("Loading default items from the source storage...");

        for (var defaultItem : source.loadAllDefaultItems()) {
            var currentName = renameIfNeeded(defaultItem.getPlainName(), source.getDataVersion());
            oldIdToItemNameMap.put(defaultItem.getInternalId(), currentName);

            if (!newItemNameToItemMap.containsKey(currentName)) {
                logger.warning(currentName + " is not found in current default items. (Renamed or removed in newer version?)");
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

    private static @NotNull List<BoxItem> loadDefaultItems(@NotNull ItemStorage storage, @Nullable MCDataVersion dataVersion, int defaultItemVersion) throws Exception {
        if (dataVersion == null) {
            return storage.saveNewDefaultItems(DefaultItemProvider.all());
        } else if (dataVersion.isSame(MCDataVersion.CURRENT)) {
            return storage.loadAllDefaultItems();
        } else {
            return DefaultItemUpdater.update(storage, dataVersion, defaultItemVersion);
        }
    }

    private static @NotNull List<BoxCustomItem> loadCustomItems(@NotNull ItemStorage storage, @Nullable MCDataVersion dataVersion) throws Exception {
        if (dataVersion == null) {
            return Collections.emptyList();
        } else if (dataVersion.isSame(MCDataVersion.CURRENT)) {
            return storage.loadAllCustomItems();
        } else {
            var items = storage.loadAllCustomItems();
            storage.updateCustomItems(items);
            return items;
        }
    }

    private static @NotNull String renameIfNeeded(@NotNull String plainName, @Nullable MCDataVersion dataVersion) {
        if (dataVersion != null && dataVersion.isBetween(MCDataVersion.MC_1_19, MCDataVersion.MC_1_19_4)) {
            if (plainName.equals("GOAT_HORN")) {
                return "PONDER_GOAT_HORN";
            }
        }

        return plainName;
    }
}
