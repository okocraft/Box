package net.okocraft.box.version.common.item;

import net.okocraft.box.storage.api.model.item.ItemData;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.jetbrains.annotations.NotNull;

public final class LegacyVersionPatches {

    public static @NotNull String goatHornName(@NotNull String original) {
        return original.equals("GOAT_HORN") ? "PONDER_GOAT_HORN" : original;
    }

    public static @NotNull ItemData goatHornData(@NotNull ItemData itemData) {
        var goatHorn = new ItemStack(Material.GOAT_HORN);
        goatHorn.editMeta(MusicInstrumentMeta.class, meta -> meta.setInstrument(MusicInstrument.PONDER));

        return new ItemData(itemData.internalId(), "PONDER_GOAT_HORN", goatHorn.serializeAsBytes());
    }

    private LegacyVersionPatches() {
        throw new UnsupportedOperationException();
    }

}
