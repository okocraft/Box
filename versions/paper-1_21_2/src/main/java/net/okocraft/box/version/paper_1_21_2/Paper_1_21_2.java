package net.okocraft.box.version.paper_1_21_2;

import net.okocraft.box.ap.annotation.version.VersionSpecific;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import net.okocraft.box.version.common.item.ItemSources;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@VersionSpecific
public final class Paper_1_21_2 {

    @VersionSpecific.Version
    public static final MCDataVersion VERSION = MCDataVersion.MC_1_21_2;

    @VersionSpecific.DefaultItemSource
    public static @NotNull Stream<DefaultItem> defaultItems() {
        return new ItemSources.Merger()
            .append(ItemSources.itemTypes())
            .append(ItemSources.potions())
            .append(ItemSources.enchantedBooks())
            .append(ItemSources.fireworks())
            .append(ItemSources.goatHorns())
            .append(ItemSources.ominousBottles())
            .result();
    }

    private Paper_1_21_2() {
        throw new UnsupportedOperationException();
    }
}
