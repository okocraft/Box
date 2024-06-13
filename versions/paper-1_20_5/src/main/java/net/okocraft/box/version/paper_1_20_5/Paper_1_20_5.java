package net.okocraft.box.version.paper_1_20_5;

import net.okocraft.box.ap.annotation.version.VersionSpecific;
import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import net.okocraft.box.version.common.item.ItemSources;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.stream.Stream;

@VersionSpecific
public final class Paper_1_20_5 {

    @VersionSpecific.Version
    public static final MCDataVersion VERSION = MCDataVersion.MC_1_20_5;

    @VersionSpecific.DefaultItemSource
    public static @NotNull Stream<DefaultItem> defaultItems() {
        return new ItemSources.Merger()
                .append(fromMaterials())
                .append(ItemSources.potions(Registry.POTION))
                .append(ItemSources.enchantedBooks(Registry.ENCHANTMENT))
                .append(ItemSources.fireworks())
                .append(ItemSources.goatHorns(Registry.INSTRUMENT))
                .result();
    }

    public static @NotNull Stream<DefaultItem> fromMaterials() {
        return Registry.MATERIAL.stream()
                .filter(Predicate.not(Material::isAir))
                .filter(Material::isItem)
                .filter(Predicate.not(material -> material.name().startsWith("LEGACY_")))
                .filter(Predicate.not(material -> material == Material.GOAT_HORN))
                .filter(Predicate.not(material -> material == Material.FIREWORK_ROCKET))
                .map(material -> new DefaultItem(ItemNameGenerator.key(material), new ItemStack(material, 1)));
    }

    private Paper_1_20_5() {
        throw new UnsupportedOperationException();
    }
}
