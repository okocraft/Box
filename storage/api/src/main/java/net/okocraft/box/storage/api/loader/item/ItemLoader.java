package net.okocraft.box.storage.api.loader.item;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxDefaultItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.factory.item.BoxItemFactory;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.item.CustomItemStorage;
import net.okocraft.box.storage.api.model.item.DefaultItemStorage;
import net.okocraft.box.storage.api.model.item.ItemData;
import net.okocraft.box.storage.api.model.item.RemappedItemStorage;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import net.okocraft.box.storage.api.model.item.provider.DefaultItemProvider;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.util.SneakyThrow;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ItemLoader {

    public static @NotNull Int2IntMap loadRemappedItemIds(@NotNull RemappedItemStorage storage) throws Exception {
        Int2IntMap oldToNew = new Int2IntOpenHashMap();
        Int2IntMap newToOld = new Int2IntOpenHashMap();

        storage.loadRemappedIds().entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .map(Map.Entry::getValue)
            .forEach(map -> {
                for (Int2IntMap.Entry entry : map.int2IntEntrySet()) {
                    int oldId = entry.getIntKey();
                    int newId = entry.getIntValue();
                    oldToNew.put(oldId, newId);
                    newToOld.put(newId, oldId);

                    if (newToOld.containsKey(oldId)) {
                        oldToNew.put(newToOld.get(oldId), newId);
                    }
                }
            });

        return oldToNew;
    }

    public static @NotNull Result fromStorage(@NotNull DefaultItemProvider itemProvider, @NotNull Storage storage) throws Exception {
        MCDataVersion dataVersion = storage.getDataVersion();
        MCDataVersion currentVersion = itemProvider.version();

        if (dataVersion != null) {
            if (dataVersion.isAfter(currentVersion)) {
                throw new IllegalStateException("Version downgrade detected! (%s -> %s)".formatted(dataVersion.dataVersion(), currentVersion.dataVersion()));
            }

            if (dataVersion.isBefore(currentVersion)) {
                BoxLogger.logger().warn("Version upgrade detected. Updating items...");
            }
        }

        Result result = new Result(
            loadDefaultItems(itemProvider, storage.defaultItemStorage(), dataVersion, remappedItems -> processRemappedItems(remappedItems, currentVersion, storage.remappedItemStorage(), storage.getStockStorage())),
            loadCustomItems(storage.customItemStorage(), dataVersion != null && !dataVersion.isSame(currentVersion))
        );

        storage.saveDataVersion(currentVersion);

        return result;
    }

    private static @NotNull List<@NotNull BoxDefaultItem> loadDefaultItems(@NotNull DefaultItemProvider itemProvider, @NotNull DefaultItemStorage defaultItemStorage, @Nullable MCDataVersion dataVersion, @NotNull Consumer<Collection<DefaultItemLoader.RemappedItem>> remappedItemsConsumer) throws Exception {
        DefaultItemLoader<DefaultItem> loader = new DefaultItemLoader<>(
            itemProvider.provide(),
            defaultItemStorage
        );

        if (dataVersion == null) {
            BoxLogger.logger().info("Initializing default items...");
            return loader.initialize((item, id) -> BoxItemFactory.createDefaultItem(id, item));
        }

        if (dataVersion.isSame(itemProvider.version())) {
            BoxLogger.logger().info("Loading default items...");
            return loader.load(
                (item, id) -> BoxItemFactory.createDefaultItem(id, item),
                item -> BoxLogger.logger().warn("Unknown default item found: {}", item)
            );
        }

        BoxLogger.logger().info("Updating default items...");
        DefaultItemLoader.UpdateResult<@NotNull BoxDefaultItem> updateResult = loader.update(
            itemProvider.renamedItems(dataVersion, itemProvider.version()),
            (item, id) -> BoxItemFactory.createDefaultItem(id, item)
        );

        remappedItemsConsumer.accept(updateResult.remappedItems());

        return updateResult.items();
    }

    private static @NotNull List<@NotNull BoxCustomItem> loadCustomItems(@NotNull CustomItemStorage storage, boolean shouldUpdate) throws Exception {
        return new CustomItemLoader(storage, shouldUpdate).load(
            item -> new ItemData(item.getInternalId(), item.getPlainName(), item.getOriginal().serializeAsBytes()),
            data -> BoxItemFactory.createCustomItem(data.internalId(), data.plainName(), ItemStack.deserializeBytes(data.itemData()))
        );
    }

    private static void processRemappedItems(@NotNull Collection<DefaultItemLoader.RemappedItem> remappedItems, @NotNull MCDataVersion version, @NotNull RemappedItemStorage remappedItemStorage, @NotNull StockStorage stockStorage) {
        Int2IntMap idMap = new Int2IntOpenHashMap();

        for (DefaultItemLoader.RemappedItem item : remappedItems) {
            idMap.put(item.oldId(), item.newId());
            try {
                remappedItemStorage.saveRemappedItem(item.oldId(), item.name(), item.newId(), version);
            } catch (Exception e) {
                SneakyThrow.sneaky(e);
            }
        }

        if (!idMap.isEmpty()) {
            try {
                stockStorage.remapItemIds(idMap);
            } catch (Exception e) {
                SneakyThrow.sneaky(e);
            }
        }
    }

    public record Result(@NotNull List<BoxDefaultItem> defaultItems, @NotNull List<BoxCustomItem> customItems) {
        public @NotNull Iterator<BoxItem> asIterator() {
            return new InitialBoxItemIterator(this.defaultItems, this.customItems);
        }

        public void logItemCount() {
            if (!this.defaultItems.isEmpty()) {
                BoxLogger.logger().info("{} default items are loaded!", this.defaultItems.size());
            }

            if (!this.customItems.isEmpty()) {
                BoxLogger.logger().info("{} custom items are loaded!", this.customItems.size());
            }
        }
    }

    private static class InitialBoxItemIterator implements Iterator<BoxItem> {

        private final Iterator<BoxDefaultItem> defaultItemIterator;
        private final Iterator<BoxCustomItem> customItemIterator;

        private boolean firstIterator = true;

        private InitialBoxItemIterator(@NotNull List<BoxDefaultItem> defaultItems, @NotNull List<BoxCustomItem> customItems) {
            this.defaultItemIterator = defaultItems.listIterator();
            this.customItemIterator = customItems.listIterator();
        }

        @Override
        public boolean hasNext() {
            if (this.firstIterator) {
                if (this.defaultItemIterator.hasNext()) {
                    return true;
                }
                this.firstIterator = false;
            }
            return this.customItemIterator.hasNext();
        }

        @Override
        public BoxItem next() {
            if (this.firstIterator) {
                return this.defaultItemIterator.next();
            } else {
                return this.customItemIterator.next();
            }
        }
    }

    private ItemLoader() {
        throw new UnsupportedOperationException();
    }
}
