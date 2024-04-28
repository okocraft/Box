package net.okocraft.box.version.paper_1_20_5;

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
                .append(ItemSources.potions(Registry.POTION))
                .append(ItemSources.enchantedBooks())
                .append(ItemSources.fireworks())
                .append(ItemSources.goatHorns())
                .result();
    }

    public static @NotNull String turtleScute(@NotNull String original) {
        return original.equals("SCUTE") ? "TURTLE_SCUTE" : original;
    }

    public static @NotNull ItemData writtenBook(@NotNull ItemData itemData) {
        if (itemData.plainName().equals("WRITTEN_BOOK")) {
            return new ItemData(itemData.internalId(), "WRITTEN_BOOK", new ItemStack(Material.WRITTEN_BOOK).serializeAsBytes());
        } else {
            return itemData;
        }
    }

    private Paper_1_20_5() {
        throw new UnsupportedOperationException();
    }
}
