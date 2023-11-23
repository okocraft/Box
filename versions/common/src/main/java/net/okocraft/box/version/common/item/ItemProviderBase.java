package net.okocraft.box.version.common.item;

import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.storage.api.util.item.patcher.ItemNamePatcher;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface ItemProviderBase {

    int version();

    @NotNull Stream<DefaultItem> defaultItemStream();

    @NotNull ItemNamePatcher itemNamePatcher();

}
