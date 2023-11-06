package net.okocraft.box.storage.api.util.item;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.ItemData;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class ItemLoader {

    public static @NotNull ItemLoadResult load(@NotNull ItemStorage storage, @NotNull DefaultItemProvider defaultItemProvider) throws Exception {
        var dataVersion = storage.getDataVersion();
        int defaultItemVersion = storage.getDefaultItemVersion();

        if (dataVersion != null && isTryingDowngrade(dataVersion, defaultItemVersion)) {
            throw new IllegalStateException("Downgrading version is not supported.");
        }

        ItemLoadResult loadResult;

        // TODO: logging
        if (dataVersion == null) {
            loadResult = initializeDefaultItems(storage, defaultItemProvider);
        } else if (dataVersion.isSame(MCDataVersion.CURRENT) && defaultItemVersion == defaultItemProvider.listVersion()) {
            loadResult = loadItems(storage);
        } else {
            loadResult = updateItems(storage, dataVersion, defaultItemProvider);
        }

        return loadResult;
    }

    public static @NotNull ItemLoadResult initializeDefaultItems(@NotNull ItemStorage storage, @NotNull DefaultItemProvider defaultItemProvider) throws Exception {
        var result = new ItemLoadResult(storage.saveDefaultItems(defaultItemProvider.provide(), Int2ObjectMaps.emptyMap()), Collections.emptyList());
        storage.saveItemVersion(MCDataVersion.CURRENT, defaultItemProvider.listVersion());
        return result;
    }

    public static @NotNull ItemLoadResult loadItems(@NotNull ItemStorage storage) throws Exception {
        return new ItemLoadResult(
                Lists.transform(storage.loadAllDefaultItems(), ItemData::toDefaultItem),
                storage.loadAllCustomItems()
        );
    }

    public static @NotNull ItemLoadResult updateItems(@NotNull ItemStorage storage, @NotNull MCDataVersion dataVersion, @NotNull DefaultItemProvider defaultItemProvider) throws Exception {
        var def = DefaultItemUpdater.update(storage, dataVersion, defaultItemProvider);
        var cus = storage.loadAllCustomItems();
        storage.updateCustomItems(cus);
        storage.saveItemVersion(MCDataVersion.CURRENT, defaultItemProvider.listVersion());
        return new ItemLoadResult(def, cus);
    }

    private static boolean isTryingDowngrade(@NotNull MCDataVersion dataVersion, int defaultItemVersion) {
        return dataVersion.isAfter(MCDataVersion.CURRENT) ||
                (dataVersion.isSame(MCDataVersion.CURRENT) && DefaultItemProvider.version() < defaultItemVersion);
    }

    public record ItemLoadResult(@NotNull List<BoxItem> defaultItems, @NotNull List<BoxCustomItem> customItems) {
        public @NotNull Iterator<BoxItem> asIterator() {
            return new InitialBoxItemIterator(this.defaultItems, this.customItems);
        }

        public void logItemCount(@NotNull Logger logger) {
            if (!this.defaultItems.isEmpty()) {
                logger.info(this.defaultItems.size() + " default items are loaded!");
            }

            if (!this.customItems.isEmpty()) {
                logger.info(this.customItems.size() + " custom items are loaded!");
            }
        }
    }

    private static class InitialBoxItemIterator implements Iterator<BoxItem> {

        private final Iterator<BoxItem> defaultItemIterator;
        private final Iterator<BoxCustomItem> customItemIterator;

        private boolean firstIterator = true;

        private InitialBoxItemIterator(@NotNull List<BoxItem> defaultItems, @NotNull List<BoxCustomItem> customItems) {
            this.defaultItemIterator = defaultItems.listIterator();
            this.customItemIterator = customItems.listIterator();
        }

        @Override
        public boolean hasNext() {
            if (firstIterator) {
                if (defaultItemIterator.hasNext()) {
                    return true;
                }
                firstIterator = false;
            }
            return customItemIterator.hasNext();
        }

        @Override
        public BoxItem next() {
            if (firstIterator) {
                return defaultItemIterator.next();
            } else {
                return customItemIterator.next();
            }
        }
    }
}
