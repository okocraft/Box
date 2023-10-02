package net.okocraft.box.core.model.loader;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.core.model.manager.BoxItemManager;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.util.item.DefaultItemProvider;
import net.okocraft.box.storage.api.util.item.DefaultItemUpdater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemLoader {

    public static @NotNull BoxItemManager load(@NotNull ItemStorage itemStorage) throws Exception {
        var dataVersion = itemStorage.getDataVersion();
        int defaultItemVersion = itemStorage.getDefaultItemVersion();

        if (dataVersion != null && isTryingDowngrade(dataVersion, defaultItemVersion)) {
            throw new IllegalStateException("Downgrading version is not supported.");
        }

        var defaultItems = loadDefaultItems(itemStorage, dataVersion, defaultItemVersion);
        var customItems = loadCustomItems(itemStorage, dataVersion);

        var logger = BoxProvider.get().getLogger();

        logger.info(defaultItems.size() + " default items are loaded!");

        if (0 < customItems.size()) {
            logger.info(customItems.size() + " custom items are loaded!");
        }

        var itemManger = new BoxItemManager(itemStorage);

        itemManger.storeItems(defaultItems);
        itemManger.storeItems(customItems);

        itemStorage.saveCurrentDataVersion();
        itemStorage.saveCurrentDefaultItemVersion();

        return itemManger;
    }

    private static @NotNull List<BoxItem> loadDefaultItems(@NotNull ItemStorage storage, @Nullable MCDataVersion dataVersion, int defaultItemVersion) throws Exception {
        var logger = BoxProvider.get().getLogger();
        logger.info("Loading default items...");

        if (dataVersion == null) {
            logger.warning("No item data found. It takes time to save default items...");
            return storage.saveNewDefaultItems(DefaultItemProvider.all());
        } else if (dataVersion.isSame(MCDataVersion.CURRENT) && defaultItemVersion == DefaultItemProvider.version()) {
            var data = storage.loadAllDefaultItems();
            var items = new ArrayList<BoxItem>(data.size());
            data.forEach(itemData -> items.add(itemData.toDefaultItem()));
            return items;
        } else {
            logger.warning("Version upgrade detected. Updating default item data...");
            return DefaultItemUpdater.update(storage, dataVersion);
        }
    }

    private static @NotNull List<BoxCustomItem> loadCustomItems(@NotNull ItemStorage storage, @Nullable MCDataVersion dataVersion) throws Exception {
        var logger = BoxProvider.get().getLogger();
        logger.info("Loading custom items...");

        if (dataVersion == null) {
            return Collections.emptyList();
        } else if (dataVersion.isSame(MCDataVersion.CURRENT)) {
            return storage.loadAllCustomItems();
        } else {
            logger.warning("Updating custom item data...");
            var items = storage.loadAllCustomItems();
            storage.updateCustomItems(items);
            return items;
        }
    }

    private static boolean isTryingDowngrade(@NotNull MCDataVersion dataVersion, int defaultItemVersion) {
        return dataVersion.isAfter(MCDataVersion.CURRENT) ||
                (dataVersion.isSame(MCDataVersion.CURRENT) && DefaultItemProvider.version() < defaultItemVersion);
    }
}
