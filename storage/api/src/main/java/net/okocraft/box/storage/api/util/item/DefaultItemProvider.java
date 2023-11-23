package net.okocraft.box.storage.api.util.item;

import net.okocraft.box.storage.api.util.item.patcher.ItemDataPatcher;
import net.okocraft.box.storage.api.util.item.patcher.ItemNamePatcher;
import net.okocraft.box.storage.api.util.item.patcher.PatcherFactory;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface DefaultItemProvider {

    @NotNull ItemVersion version();

    @NotNull Stream<DefaultItem> provide();

    @NotNull PatcherFactory<ItemNamePatcher> itemNamePatcherFactory();

    @NotNull PatcherFactory<ItemDataPatcher> itemDataPatcherFactory();

}
