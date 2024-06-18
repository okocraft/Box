package net.okocraft.box.version.paper_1_20_6;

import net.okocraft.box.ap.annotation.version.VersionSpecific;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import net.okocraft.box.version.paper_1_20_5.Paper_1_20_5;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@VersionSpecific
public final class Paper_1_20_6 {

    @VersionSpecific.Version
    public static final MCDataVersion VERSION = MCDataVersion.MC_1_20_6;

    @VersionSpecific.DefaultItemSource
    public static @NotNull Stream<DefaultItem> defaultItems() {
        return Paper_1_20_5.defaultItems();
    }

    private Paper_1_20_6() {
        throw new UnsupportedOperationException();
    }
}
