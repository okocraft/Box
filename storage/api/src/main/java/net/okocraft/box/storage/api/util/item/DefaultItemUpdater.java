package net.okocraft.box.storage.api.util.item;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DefaultItemUpdater {

    public static @NotNull List<BoxItem> update(@NotNull ItemStorage storage, @NotNull MCDataVersion dataVersion) throws Exception {
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

        if (dataVersion.isAfterOrSame(MCDataVersion.MC_1_19)) {
            var oldGoatHorn = oldItemMap.get(new ItemStack(Material.GOAT_HORN));

            if (oldGoatHorn != null) {
                var newGoatHorn = DefaultItemProvider.createPonderGoatHorn();
                oldToNewItemMap.put(oldGoatHorn, newGoatHorn);
                newItems.remove(newGoatHorn);
            }
        }

        var defaultItems = new ArrayList<BoxItem>();

        defaultItems.addAll(storage.updateDefaultItems(oldToNewItemMap));
        defaultItems.addAll(storage.saveNewDefaultItems(newItems));

        return defaultItems;
    }

    private DefaultItemUpdater() {
        throw new UnsupportedOperationException();
    }
}
