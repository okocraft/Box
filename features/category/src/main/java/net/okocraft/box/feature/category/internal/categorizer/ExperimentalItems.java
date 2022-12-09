package net.okocraft.box.feature.category.internal.categorizer;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

public final class ExperimentalItems {

    /*
     * This is extracted Materials that marked as @ApiStatus.Experimental.
     * Let's hope that an appropriate API will be provided in the future.
     *
     * NOTE: Material#Bundle has existed since 1.17, so it is not included here.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull Set<Material> mc1_19_3() {
        return EnumSet.of(
                Material.ACACIA_HANGING_SIGN, Material.BAMBOO_BLOCK, Material.BAMBOO_BUTTON,
                Material.BAMBOO_CHEST_RAFT, Material.BAMBOO_DOOR, Material.BAMBOO_FENCE,
                Material.BAMBOO_FENCE_GATE, Material.BAMBOO_HANGING_SIGN, Material.BAMBOO_MOSAIC,
                Material.BAMBOO_MOSAIC_SLAB, Material.BAMBOO_MOSAIC_STAIRS, Material.BAMBOO_PLANKS,
                Material.BAMBOO_PRESSURE_PLATE, Material.BAMBOO_RAFT, Material.BAMBOO_SIGN,
                Material.BAMBOO_SLAB, Material.BAMBOO_STAIRS, Material.BAMBOO_TRAPDOOR,
                Material.BIRCH_HANGING_SIGN, Material.CAMEL_SPAWN_EGG, Material.CHISELED_BOOKSHELF,
                Material.CRIMSON_HANGING_SIGN, Material.DARK_OAK_HANGING_SIGN, Material.JUNGLE_HANGING_SIGN,
                Material.MANGROVE_HANGING_SIGN, Material.OAK_HANGING_SIGN, Material.PIGLIN_HEAD,
                Material.SPRUCE_HANGING_SIGN, Material.STRIPPED_BAMBOO_BLOCK, Material.WARPED_HANGING_SIGN
        );
    }
}
