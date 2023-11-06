package net.okocraft.box.storage.api.util.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.ItemData;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DefaultItemUpdater {

    public static @NotNull List<BoxItem> update(@NotNull ItemStorage storage, @NotNull MCDataVersion dataVersion, @NotNull DefaultItemProvider defaultItemProvider) throws Exception {
        var oldItemMap = storage.loadAllDefaultItems().stream().collect(Collectors.toMap(DefaultItemUpdater::toItemStack, Function.identity()));

        for (var patchEntry : defaultItemProvider.itemPatchMap(dataVersion).entrySet()) {
            var oldItemStack = patchEntry.getKey();
            var oldItem = oldItemMap.remove(oldItemStack);

            if (oldItem != null) {
                var newItemStack = patchEntry.getValue();
                oldItemMap.put(newItemStack, new ItemData(oldItem.internalId(), oldItem.plainName(), newItemStack.serializeAsBytes()));
            }
        }

        var newItems = new ArrayList<DefaultItem>();
        var updatedItemMap = new Int2ObjectOpenHashMap<DefaultItem>();

        for (var item : defaultItemProvider.provide()) {
            var oldItem = oldItemMap.remove(item.itemStack());

            if (oldItem == null) {
                newItems.add(item);
            } else {
                updatedItemMap.put(oldItem.internalId(), item);
            }
        }

        return storage.saveDefaultItems(newItems, updatedItemMap);
    }

    private static @NotNull ItemStack toItemStack(@NotNull ItemData data) {
        return ItemStack.deserializeBytes(data.itemData());
    }

    private DefaultItemUpdater() {
        throw new UnsupportedOperationException();
    }
}
