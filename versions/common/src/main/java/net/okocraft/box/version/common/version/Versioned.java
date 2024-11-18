package net.okocraft.box.version.common.version;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface Versioned {

    static List<Versioned> implementations(ClassLoader classLoader) {
        return ServiceLoader.load(Versioned.class, classLoader).stream().map(ServiceLoader.Provider::get).toList();
    }

    MCDataVersion version();

    Stream<DefaultItem> defaultItems();
}
