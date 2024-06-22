package net.okocraft.box.version.paper_1_20_5;

import net.okocraft.box.ap.annotation.version.VersionSpecific;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import net.okocraft.box.version.common.item.ItemSources;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@VersionSpecific
public final class Paper_1_20_5 {

    @VersionSpecific.Version
    public static final MCDataVersion VERSION = MCDataVersion.MC_1_20_5;

    @VersionSpecific.DefaultItemSource
    public static @NotNull Stream<DefaultItem> defaultItems() {
        return new ItemSources.Merger()
                .append(
                        ItemSources.materials(Registry.MATERIAL)
                                .filter(ItemSources.NOT_GOAT_HORN)
                                .filter(ItemSources.NOT_FIREWORK)
                                .map(ItemSources::toDefaultItem)
                )
                .append(ItemSources.potions(Registry.POTION))
                .append(ItemSources.enchantedBooks(Registry.ENCHANTMENT))
                .append(ItemSources.fireworks())
                .append(ItemSources.goatHorns(Registry.INSTRUMENT))
                .result();
    }

    private Paper_1_20_5() {
        throw new UnsupportedOperationException();
    }
}
