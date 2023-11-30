package net.okocraft.box.storage.api.util.item;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.storage.api.model.item.ItemData;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.util.item.patcher.ItemDataPatcher;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ItemLoader {

    public static @NotNull ItemLoadResult load(@NotNull ItemStorage storage,
                                               @NotNull DefaultItemProvider defaultItemProvider) throws Exception {
        var currentVersion = defaultItemProvider.version();
        var storageItemVersion = storage.getItemVersion();

        if (storageItemVersion.isPresent() && currentVersion.isTryingDowngrade(storageItemVersion.get())) {
            throw new IllegalStateException("Downgrading version is not supported.");
        }

        ItemLoadResult loadResult;

        // TODO: logging
        if (storageItemVersion.isEmpty()) {
            loadResult = initializeDefaultItems(storage, currentVersion, defaultItemProvider.provide());
        } else if (currentVersion.equals(storageItemVersion.get())) {
            loadResult = loadItems(storage);
        } else {
            loadResult = updateItems(storage, currentVersion, defaultItemProvider.itemDataPatcherFactory().create(storageItemVersion.get()), defaultItemProvider.provide());
        }

        return loadResult;
    }

    public static @NotNull ItemLoadResult initializeDefaultItems(@NotNull ItemStorage storage,
                                                                 @NotNull ItemVersion currentVersion,
                                                                 @NotNull Stream<DefaultItem> defaultItemStream) throws Exception {
        var result = new ItemLoadResult(storage.saveDefaultItems(defaultItemStream.toList(), Int2ObjectMaps.emptyMap()), Collections.emptyList());
        storage.saveItemVersion(currentVersion);
        return result;
    }

    public static @NotNull ItemLoadResult loadItems(@NotNull ItemStorage storage) throws Exception {
        return new ItemLoadResult(
                Lists.transform(storage.loadAllDefaultItems(), ItemData::toDefaultItem),
                storage.loadAllCustomItems()
        );
    }

    public static @NotNull ItemLoadResult updateItems(@NotNull ItemStorage storage,
                                                      @NotNull ItemVersion currentVersion,
                                                      @NotNull ItemDataPatcher itemDataPatcher,
                                                      @NotNull Stream<DefaultItem> defaultItemStream) throws Exception {
        var defaultItems = updateDefaultItems(storage, itemDataPatcher, defaultItemStream);
        var customItems = storage.loadAllCustomItems();
        storage.updateCustomItems(customItems);
        storage.saveItemVersion(currentVersion);
        return new ItemLoadResult(defaultItems, customItems);
    }

    private static @NotNull List<BoxItem> updateDefaultItems(@NotNull ItemStorage storage,
                                                             @NotNull ItemDataPatcher itemDataPatcher,
                                                             @NotNull Stream<DefaultItem> defaultItemStream) throws Exception {
        var loadedItemData = storage.loadAllDefaultItems();
        var oldItemMap = new HashMap<ItemStack, ItemData>();

        for (var itemData : loadedItemData) {
            var patched = itemDataPatcher.patch(itemData);
            oldItemMap.put(ItemStack.deserializeBytes(patched.itemData()), itemData);
        }

        var newItems = new ArrayList<DefaultItem>();
        var updatedItemMap = new Int2ObjectOpenHashMap<DefaultItem>();

        defaultItemStream.forEach(item -> {
            var oldItem = oldItemMap.remove(item.itemStack());

            if (oldItem == null) {
                newItems.add(item);
            } else {
                updatedItemMap.put(oldItem.internalId(), item);
            }
        });

        return storage.saveDefaultItems(newItems, updatedItemMap);
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
