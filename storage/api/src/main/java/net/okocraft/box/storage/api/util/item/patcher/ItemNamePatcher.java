package net.okocraft.box.storage.api.util.item.patcher;

import org.jetbrains.annotations.NotNull;

public interface ItemNamePatcher {

    ItemNamePatcher NOOP = original -> original;

    @NotNull String renameIfNeeded(@NotNull String original);

}
