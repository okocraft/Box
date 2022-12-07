package net.okocraft.box.feature.category.internal.categorizer;

import net.okocraft.box.feature.category.internal.category.CommonDefaultCategory;
import net.okocraft.box.feature.category.internal.category.DefaultCategory;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class MC119Categorizer {

    static @Nullable DefaultCategory categorize(@NotNull Material type) {
        if (type.name().startsWith("MUD_") || type == Material.PACKED_MUD) {
            return CommonDefaultCategory.DIRT;
        }

        if (type.name().contains("SCULK")) {
            return DefaultCategory.create("sculk", Material.SCULK);
        }

        return switch (type) {
            case OCHRE_FROGLIGHT,PEARLESCENT_FROGLIGHT,TADPOLE_BUCKET, VERDANT_FROGLIGHT -> CommonDefaultCategory.OCEANS;
            case ECHO_SHARD -> CommonDefaultCategory.MISC;
            case FROGSPAWN -> CommonDefaultCategory.UNAVAILABLE;
            case GOAT_HORN -> CommonDefaultCategory.MOB_DROPS;
            case RECOVERY_COMPASS -> CommonDefaultCategory.TOOLS;
            case DISC_FRAGMENT_5 -> CommonDefaultCategory.MUSIC_DISCS;
            case MANGROVE_ROOTS -> CommonDefaultCategory.WOODS;
            default -> null;
        };
    }

    private MC119Categorizer() {
        throw new UnsupportedOperationException();
    }
}
