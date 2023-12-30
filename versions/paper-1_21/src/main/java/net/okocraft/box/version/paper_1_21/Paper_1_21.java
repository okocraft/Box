package net.okocraft.box.version.paper_1_21;

import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.version.common.item.ItemSources;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public final class Paper_1_21 {

    public static @NotNull Stream<DefaultItem> defaultItems() {
        return new ItemSources.Merger()
                .append(
                        ItemSources.materials()
                                .filter(ItemSources.NOT_GOAT_HORN)
                                .filter(ItemSources.experimentalMaterialFilter(Bukkit.getWorlds().get(0)).negate())
                                .map(ItemSources::toDefaultItem)
                )
                .append(ItemSources.potions())
                .append(ItemSources.enchantedBooks())
                .append(ItemSources.fireworks())
                .append(ItemSources.goatHorns())
                .result();
    }

    private Paper_1_21() {
        throw new UnsupportedOperationException();
    }
}
