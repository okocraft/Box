package net.okocraft.box.storage.migrator.util;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.IntFunction;

public final class MigratedBoxItemSupplier implements IntFunction<BoxItem> {

    private final Map<Integer, String> oldIdToItemNameMap;
    private final Map<String, BoxItem> newItemNameToItemMap;

    public MigratedBoxItemSupplier(@NotNull Map<Integer, String> oldIdToItemNameMap, @NotNull Map<String, BoxItem> newItemNameToItemMap) {
        this.oldIdToItemNameMap = oldIdToItemNameMap;
        this.newItemNameToItemMap = newItemNameToItemMap;
    }

    @Override
    public @Nullable BoxItem apply(int id) {
        var itemName = oldIdToItemNameMap.get(id);
        return itemName != null ? newItemNameToItemMap.get(itemName) : null;
    }
}
