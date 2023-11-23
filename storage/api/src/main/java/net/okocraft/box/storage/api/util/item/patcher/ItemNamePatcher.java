package net.okocraft.box.storage.api.util.item.patcher;

import org.jetbrains.annotations.NotNull;

public interface ItemNamePatcher {

    ItemNamePatcher NOOP = original -> original;

    @NotNull String renameIfNeeded(@NotNull String original);

    default @NotNull ItemNamePatcher andThen(@NotNull ItemNamePatcher next) {
        return original -> next.renameIfNeeded(this.renameIfNeeded(original));
    }

}
