package net.okocraft.box.storage.api.loader.item;

import net.okocraft.box.storage.api.model.item.CustomItemStorage;
import net.okocraft.box.storage.api.model.item.ItemData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

record CustomItemLoader(@NotNull CustomItemStorage storage, boolean shouldUpdateData) {
    <R> @NotNull List<R> load(@NotNull Function<R, ItemData> serializer, @NotNull Function<ItemData, R> deserializer) throws Exception {
        var loaded = new ArrayList<R>();
        this.storage.loadItemData(data -> loaded.add(deserializer.apply(data)));

        if (!loaded.isEmpty() && this.shouldUpdateData) {
            this.storage.updateItemData(loaded.stream().map(serializer));
        }

        return loaded;
    }
}
