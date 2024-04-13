package net.okocraft.box.version.paper_1_20_5;

import net.okocraft.box.api.model.item.ItemVersion;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.version.common.item.ItemSources;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public final class Paper_1_20_5 {

    public static final ItemVersion VERSION = new ItemVersion(MCDataVersion.MC_1_20_5, 0);

    public static @NotNull Stream<DefaultItem> defaultItems() {
        return new ItemSources.Merger()
                .append(
                        ItemSources.materials()
                                .filter(ItemSources.NOT_GOAT_HORN)
                                .filter(ItemSources.createEnabledItemFilter(Bukkit.getWorlds().getFirst()))
                                .map(ItemSources::toDefaultItem)
                )
                .append(ItemSources.potions())
                .append(ItemSources.enchantedBooks())
                .append(ItemSources.fireworks())
                .append(ItemSources.goatHorns())
                .result();
    }

    private Paper_1_20_5() {
        throw new UnsupportedOperationException();
    }
}
