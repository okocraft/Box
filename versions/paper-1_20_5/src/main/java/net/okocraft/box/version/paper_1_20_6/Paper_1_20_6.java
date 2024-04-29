package net.okocraft.box.version.paper_1_20_6;

import net.okocraft.box.ap.annotation.source.DefaultItemSource;
import net.okocraft.box.ap.annotation.version.DefaultItemVersion;
import net.okocraft.box.ap.annotation.version.VersionSpecific;
import net.okocraft.box.api.model.item.ItemVersion;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.version.paper_1_20_5.Paper_1_20_5;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@VersionSpecific
public final class Paper_1_20_6 {

    @DefaultItemVersion
    public static final ItemVersion VERSION = new ItemVersion(MCDataVersion.MC_1_20_6, 0);

    @DefaultItemSource
    public static @NotNull Stream<DefaultItem> defaultItems() {
        return Paper_1_20_5.defaultItems();
    }

    private Paper_1_20_6() {
        throw new UnsupportedOperationException();
    }
}
