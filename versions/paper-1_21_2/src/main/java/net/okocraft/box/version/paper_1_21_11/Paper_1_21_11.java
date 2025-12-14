package net.okocraft.box.version.paper_1_21_11;

import net.okocraft.box.ap.annotation.version.VersionSpecific;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import net.okocraft.box.version.paper_1_21_2.Paper_1_21_2;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@VersionSpecific
public final class Paper_1_21_11 {

    @VersionSpecific.Version
    public static final MCDataVersion VERSION = MCDataVersion.MC_1_21_11;

    @VersionSpecific.DefaultItemSource
    public static @NotNull Stream<DefaultItem> defaultItems() {
        return Paper_1_21_2.defaultItems();
    }

    private Paper_1_21_11() {
        throw new UnsupportedOperationException();
    }
}
