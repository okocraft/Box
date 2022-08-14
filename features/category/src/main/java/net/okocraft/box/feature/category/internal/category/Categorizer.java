package net.okocraft.box.feature.category.internal.category;

import net.okocraft.box.feature.category.internal.util.MCDataVersion;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Categorizer {

    public static @Nullable DefaultCategory categorize(@NotNull ItemStack item) {
        var byTags = CommonCategorizer.checkTags(item);

        if (byTags != null) {
            return byTags;
        }

        var byMaterials = CommonCategorizer.checkMaterial(item.getType());

        if (byMaterials != null) {
            return byMaterials;
        }

        if (MCDataVersion.CURRENT.isAfterOrSame(MCDataVersion.MC_1_19)) {
            return MC119Categorizer.categorize(item.getType());
        }

        return null;
    }

    private Categorizer() {
        throw new UnsupportedOperationException();
    }
}
