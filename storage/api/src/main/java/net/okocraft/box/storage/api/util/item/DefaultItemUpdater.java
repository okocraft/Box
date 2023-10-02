package net.okocraft.box.storage.api.util.item;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.factory.item.BoxItemFactory;
import net.okocraft.box.storage.api.model.item.ItemData;
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
        var oldItemMap = storage.loadAllDefaultItems().stream().map(DefaultItemUpdater::fixItemStack).collect(Collectors.toMap(BoxItem::getOriginal, Function.identity()));
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

    private static @NotNull BoxItem fixItemStack(@NotNull ItemData data) {
        if (MCDataVersion.CURRENT.isBefore(MCDataVersion.MC_1_20_2)) {
            return data.toDefaultItem();
        }

        var item = switch (data.plainName()) {
            case "POTION" -> new ItemStack(Material.POTION);
            case "LINGERING_POTION" -> new ItemStack(Material.LINGERING_POTION);
            case "SPLASH_POTION" -> new ItemStack(Material.SPLASH_POTION);
            case "TIPPED_ARROW" -> new ItemStack(Material.TIPPED_ARROW);
            case "SUSPICIOUS_STEW" -> new ItemStack(Material.SUSPICIOUS_STEW);
            default -> ItemStack.deserializeBytes(data.itemData());
        };

        return BoxItemFactory.createDefaultItem(item, data.plainName(), data.internalId());
    }

    private DefaultItemUpdater() {
        throw new UnsupportedOperationException();
    }
}
