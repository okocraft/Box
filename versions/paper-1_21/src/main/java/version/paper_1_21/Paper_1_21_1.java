package version.paper_1_21;

import net.okocraft.box.ap.annotation.version.VersionSpecific;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@VersionSpecific
public final class Paper_1_21_1 {

    @VersionSpecific.Version
    public static final MCDataVersion VERSION = MCDataVersion.MC_1_21_1;

    @VersionSpecific.DefaultItemSource
    public static @NotNull Stream<DefaultItem> defaultItems() {
        return Paper_1_21.defaultItems();
    }

    private Paper_1_21_1() {
        throw new UnsupportedOperationException();
    }
}
