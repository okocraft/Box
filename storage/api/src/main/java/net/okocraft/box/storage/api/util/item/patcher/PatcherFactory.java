package net.okocraft.box.storage.api.util.item.patcher;

import net.okocraft.box.api.model.item.ItemVersion;
import org.jetbrains.annotations.NotNull;

public interface PatcherFactory<P> {

    @NotNull P create(@NotNull ItemVersion startingVersion);

}
