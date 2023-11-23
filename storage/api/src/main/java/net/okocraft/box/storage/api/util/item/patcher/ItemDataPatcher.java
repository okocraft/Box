package net.okocraft.box.storage.api.util.item.patcher;

import net.okocraft.box.storage.api.model.item.ItemData;
import org.jetbrains.annotations.NotNull;

public interface ItemDataPatcher {

    ItemDataPatcher NOOP = original -> original;

    @NotNull ItemData patch(@NotNull ItemData original);

    default @NotNull ItemDataPatcher andThen(@NotNull ItemDataPatcher next) {
        return original -> next.patch(this.patch(original));
    }

}
