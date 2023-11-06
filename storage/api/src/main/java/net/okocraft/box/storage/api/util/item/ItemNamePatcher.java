package net.okocraft.box.storage.api.util.item;

import org.jetbrains.annotations.NotNull;

public interface ItemNamePatcher {

    @NotNull String renameIfNeeded(@NotNull String original);

    default @NotNull ItemNamePatcher andThen(@NotNull ItemNamePatcher next) {
        return original -> next.renameIfNeeded(this.renameIfNeeded(original));
    }

}
