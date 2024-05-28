package net.okocraft.box.version.paper_1_20_5;

import net.okocraft.box.ap.annotation.patch.ItemDataPatch;
import net.okocraft.box.ap.annotation.patch.ItemNamePatch;
import net.okocraft.box.ap.annotation.source.DefaultItemSource;
import net.okocraft.box.ap.annotation.version.DefaultItemVersion;
import net.okocraft.box.ap.annotation.version.VersionSpecific;
import net.okocraft.box.api.model.item.ItemVersion;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.ItemData;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.version.common.item.ItemSources;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@VersionSpecific
public final class Paper_1_20_5 {

    @DefaultItemVersion
    @ItemNamePatch.Rename(oldName = "SCUTE", newName = "TURTLE_SCUTE")
    public static final ItemVersion VERSION = new ItemVersion(MCDataVersion.MC_1_20_5, 0);

    @DefaultItemSource
    public static @NotNull Stream<DefaultItem> defaultItems() {
        return new ItemSources.Merger()
                .append(
                        ItemSources.materials(Registry.MATERIAL)
                                .filter(ItemSources.NOT_GOAT_HORN)
                                .filter(ItemSources.createEnabledItemFilter(Bukkit.getWorlds().getFirst()))
                                .map(ItemSources::toDefaultItem)
                )
                .append(ItemSources.potions(Registry.POTION))
                .append(ItemSources.enchantedBooks(Registry.ENCHANTMENT))
                .append(ItemSources.fireworks())
                .append(ItemSources.goatHorns(Registry.INSTRUMENT))
                .result();
    }

    @ItemDataPatch.UpdateItemData(targets = "WRITTEN_BOOK")
    public static @NotNull ItemData writtenBook(@NotNull ItemData itemData) {
        return new ItemData(itemData.internalId(), itemData.plainName(), new ItemStack(Material.WRITTEN_BOOK).serializeAsBytes());
    }

    private Paper_1_20_5() {
        throw new UnsupportedOperationException();
    }
}
