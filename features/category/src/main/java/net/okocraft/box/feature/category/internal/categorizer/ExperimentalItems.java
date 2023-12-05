package net.okocraft.box.feature.category.internal.categorizer;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.EnumSet;
import java.util.Set;

public final class ExperimentalItems {

    /*
     * This is extracted Materials that marked as @ApiStatus.Experimental.
     * Let's hope that an appropriate API will be provided in the future.
     *
     * NOTE: Material#BUNDLE has existed since 1.17, so it is not included here.
     */
    public static @NotNull @Unmodifiable Set<Material> mc1_19_3() {
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

    /*
     * This is extracted Materials that marked as @ApiStatus.Experimental.
     * Let's hope that an appropriate API will be provided in the future.
     *
     * NOTE: Material#BUNDLE has existed since 1.17, so it is not included here.
     */
    public static @NotNull @Unmodifiable Set<Material> mc1_19_4() {
        return EnumSet.of(
                Material.CHERRY_PLANKS, Material.BAMBOO_PLANKS, Material.BAMBOO_MOSAIC,
                Material.CHERRY_SAPLING, Material.SUSPICIOUS_SAND, Material.CHERRY_LOG, Material.BAMBOO_BLOCK,
                Material.STRIPPED_CHERRY_LOG, Material.STRIPPED_CHERRY_WOOD, Material.STRIPPED_BAMBOO_BLOCK,
                Material.CHERRY_WOOD, Material.CHERRY_LEAVES, Material.TORCHFLOWER, Material.PINK_PETALS,
                Material.CHERRY_SLAB, Material.BAMBOO_SLAB, Material.BAMBOO_MOSAIC_SLAB, Material.CHISELED_BOOKSHELF,
                Material.DECORATED_POT, Material.CHERRY_FENCE, Material.BAMBOO_FENCE, Material.CHERRY_STAIRS,
                Material.BAMBOO_STAIRS, Material.BAMBOO_MOSAIC_STAIRS, Material.CHERRY_BUTTON,
                Material.BAMBOO_BUTTON, Material.CHERRY_PRESSURE_PLATE, Material.BAMBOO_PRESSURE_PLATE,
                Material.CHERRY_DOOR, Material.BAMBOO_DOOR, Material.CHERRY_TRAPDOOR, Material.BAMBOO_TRAPDOOR,
                Material.CHERRY_FENCE_GATE, Material.BAMBOO_FENCE_GATE, Material.CHERRY_BOAT,
                Material.CHERRY_CHEST_BOAT, Material.BAMBOO_RAFT, Material.BAMBOO_CHEST_RAFT, Material.CHERRY_SIGN,
                Material.BAMBOO_SIGN, Material.OAK_HANGING_SIGN, Material.SPRUCE_HANGING_SIGN,
                Material.BIRCH_HANGING_SIGN, Material.JUNGLE_HANGING_SIGN, Material.ACACIA_HANGING_SIGN,
                Material.CHERRY_HANGING_SIGN, Material.DARK_OAK_HANGING_SIGN, Material.MANGROVE_HANGING_SIGN,
                Material.BAMBOO_HANGING_SIGN, Material.CRIMSON_HANGING_SIGN, Material.WARPED_HANGING_SIGN,
                Material.CAMEL_SPAWN_EGG, Material.SNIFFER_SPAWN_EGG, Material.PIGLIN_HEAD, Material.TORCHFLOWER_SEEDS,
                Material.SUSPICIOUS_STEW, Material.BRUSH, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
                // POTTERY_SHARD_* have been renamed in 1.20
                Material.valueOf("POTTERY_SHARD_ARCHER"),
                Material.valueOf("POTTERY_SHARD_PRIZE"),
                Material.valueOf("POTTERY_SHARD_ARMS_UP"),
                Material.valueOf("POTTERY_SHARD_SKULL")
        );
    }

    /*
     * This is extracted Materials that marked as @MinecraftExperimental.
     * Let's hope that an appropriate API will be provided in the future.
     */
    public static @NotNull @Unmodifiable Set<Material> mc1_20_3() {
        // FIXME: remove valueOf
        return EnumSet.of(
                Material.valueOf("TUFF_SLAB"),
                Material.valueOf("TUFF_STAIRS"),
                Material.valueOf("TUFF_WALL"),
                Material.valueOf("CHISELED_TUFF"),
                Material.valueOf("POLISHED_TUFF"),
                Material.valueOf("POLISHED_TUFF_SLAB"),
                Material.valueOf("POLISHED_TUFF_STAIRS"),
                Material.valueOf("POLISHED_TUFF_WALL"),
                Material.valueOf("TUFF_BRICKS"),
                Material.valueOf("TUFF_BRICK_SLAB"),
                Material.valueOf("TUFF_BRICK_STAIRS"),
                Material.valueOf("TUFF_BRICK_WALL"),
                Material.valueOf("CHISELED_TUFF_BRICKS"),
                Material.valueOf("CHISELED_COPPER"),
                Material.valueOf("EXPOSED_CHISELED_COPPER"),
                Material.valueOf("WEATHERED_CHISELED_COPPER"),
                Material.valueOf("OXIDIZED_CHISELED_COPPER"),
                Material.valueOf("WAXED_CHISELED_COPPER"),
                Material.valueOf("WAXED_EXPOSED_CHISELED_COPPER"),
                Material.valueOf("WAXED_WEATHERED_CHISELED_COPPER"),
                Material.valueOf("WAXED_OXIDIZED_CHISELED_COPPER"),
                Material.valueOf("COPPER_DOOR"),
                Material.valueOf("EXPOSED_COPPER_DOOR"),
                Material.valueOf("WEATHERED_COPPER_DOOR"),
                Material.valueOf("OXIDIZED_COPPER_DOOR"),
                Material.valueOf("WAXED_COPPER_DOOR"),
                Material.valueOf("WAXED_EXPOSED_COPPER_DOOR"),
                Material.valueOf("WAXED_WEATHERED_COPPER_DOOR"),
                Material.valueOf("WAXED_OXIDIZED_COPPER_DOOR"),
                Material.valueOf("COPPER_TRAPDOOR"),
                Material.valueOf("EXPOSED_COPPER_TRAPDOOR"),
                Material.valueOf("WEATHERED_COPPER_TRAPDOOR"),
                Material.valueOf("OXIDIZED_COPPER_TRAPDOOR"),
                Material.valueOf("WAXED_COPPER_TRAPDOOR"),
                Material.valueOf("WAXED_EXPOSED_COPPER_TRAPDOOR"),
                Material.valueOf("WAXED_WEATHERED_COPPER_TRAPDOOR"),
                Material.valueOf("WAXED_OXIDIZED_COPPER_TRAPDOOR"),
                Material.valueOf("CRAFTER"),
                Material.valueOf("BREEZE_SPAWN_EGG"),
                Material.valueOf("COPPER_GRATE"),
                Material.valueOf("EXPOSED_COPPER_GRATE"),
                Material.valueOf("WEATHERED_COPPER_GRATE"),
                Material.valueOf("OXIDIZED_COPPER_GRATE"),
                Material.valueOf("WAXED_COPPER_GRATE"),
                Material.valueOf("WAXED_EXPOSED_COPPER_GRATE"),
                Material.valueOf("WAXED_WEATHERED_COPPER_GRATE"),
                Material.valueOf("WAXED_OXIDIZED_COPPER_GRATE"),
                Material.valueOf("COPPER_BULB"),
                Material.valueOf("EXPOSED_COPPER_BULB"),
                Material.valueOf("WEATHERED_COPPER_BULB"),
                Material.valueOf("OXIDIZED_COPPER_BULB"),
                Material.valueOf("WAXED_COPPER_BULB"),
                Material.valueOf("WAXED_EXPOSED_COPPER_BULB"),
                Material.valueOf("WAXED_WEATHERED_COPPER_BULB"),
                Material.valueOf("WAXED_OXIDIZED_COPPER_BULB"),
                Material.valueOf("TRIAL_SPAWNER"),
                Material.valueOf("TRIAL_KEY")
        );
    }
}
