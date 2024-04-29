package net.okocraft.box.version.common.version;

import net.okocraft.box.api.model.item.ItemVersion;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.storage.api.util.item.patcher.ItemDataPatcher;
import net.okocraft.box.storage.api.util.item.patcher.ItemNamePatcher;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface Versioned {

    static List<Versioned> implementations(ClassLoader classLoader) {
        return ServiceLoader.load(Versioned.class, classLoader).stream().map(ServiceLoader.Provider::get).toList();
    }

    ItemVersion defaultItemVersion();

    Stream<DefaultItem> defaultItems();

    Stream<ItemNamePatcher> itemNamePatchers();

    Stream<ItemDataPatcher> itemDataPatchers();

}
