package net.okocraft.box.storage.api.util.item.patcher;

import net.okocraft.box.storage.api.model.item.ItemData;
import org.jetbrains.annotations.NotNull;

public interface ItemDataPatcher {

    ItemDataPatcher NOOP = original -> original;

    @NotNull ItemData patch(@NotNull ItemData original);

}
