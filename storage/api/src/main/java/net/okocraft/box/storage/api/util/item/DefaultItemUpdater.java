package net.okocraft.box.storage.api.util.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.ItemData;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DefaultItemUpdater {

    public static @NotNull List<BoxItem> update(@NotNull ItemStorage storage, @NotNull MCDataVersion dataVersion) throws Exception {
        var oldItemMap = storage.loadAllDefaultItems().stream().collect(Collectors.toMap(DefaultItemUpdater::toItemStack, Function.identity()));

        var newItems = new ArrayList<DefaultItem>();
        var updatedItemMap = new Int2ObjectOpenHashMap<DefaultItem>();

        for (var item : DefaultItemProvider.all()) {
            var oldItem = oldItemMap.get(item.itemStack());

            if (oldItem == null) {
                newItems.add(item);
            } else {
                updatedItemMap.put(oldItem.internalId(), item);
            }
        }

        if (dataVersion.isAfterOrSame(MCDataVersion.MC_1_19)) {
            var oldGoatHorn = oldItemMap.get(new ItemStack(Material.GOAT_HORN));

            if (oldGoatHorn != null) {
                var newGoatHorn = DefaultItemProvider.createPonderGoatHorn();
                updatedItemMap.put(oldGoatHorn.internalId(), newGoatHorn);
                newItems.remove(newGoatHorn);
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
