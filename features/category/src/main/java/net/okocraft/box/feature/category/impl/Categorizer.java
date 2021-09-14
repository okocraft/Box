package net.okocraft.box.feature.category.impl;

import com.destroystokyo.paper.MaterialTags;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Function;

import static com.destroystokyo.paper.MaterialTags.ARROWS;
import static com.destroystokyo.paper.MaterialTags.AXES;
import static com.destroystokyo.paper.MaterialTags.BOOTS;
import static com.destroystokyo.paper.MaterialTags.BOWS;
import static com.destroystokyo.paper.MaterialTags.CHESTPLATES;
import static com.destroystokyo.paper.MaterialTags.CHEST_EQUIPPABLE;
import static com.destroystokyo.paper.MaterialTags.CONCRETES;
import static com.destroystokyo.paper.MaterialTags.CONCRETE_POWDER;
import static com.destroystokyo.paper.MaterialTags.COOKED_FISH;
import static com.destroystokyo.paper.MaterialTags.CORAL;
import static com.destroystokyo.paper.MaterialTags.CORAL_BLOCKS;
import static com.destroystokyo.paper.MaterialTags.CORAL_FANS;
import static com.destroystokyo.paper.MaterialTags.DOORS;
import static com.destroystokyo.paper.MaterialTags.DYES;
import static com.destroystokyo.paper.MaterialTags.FENCE_GATES;
import static com.destroystokyo.paper.MaterialTags.FISH_BUCKETS;
import static com.destroystokyo.paper.MaterialTags.GLASS;
import static com.destroystokyo.paper.MaterialTags.GLASS_PANES;
import static com.destroystokyo.paper.MaterialTags.GLAZED_TERRACOTTA;
import static com.destroystokyo.paper.MaterialTags.GOLDEN_APPLES;
import static com.destroystokyo.paper.MaterialTags.HEAD_EQUIPPABLE;
import static com.destroystokyo.paper.MaterialTags.HELMETS;
import static com.destroystokyo.paper.MaterialTags.HOES;
import static com.destroystokyo.paper.MaterialTags.HORSE_ARMORS;
import static com.destroystokyo.paper.MaterialTags.LEGGINGS;
import static com.destroystokyo.paper.MaterialTags.MUSHROOMS;
import static com.destroystokyo.paper.MaterialTags.MUSHROOM_BLOCKS;
import static com.destroystokyo.paper.MaterialTags.MUSIC_DISCS;
import static com.destroystokyo.paper.MaterialTags.PICKAXES;
import static com.destroystokyo.paper.MaterialTags.PISTONS;
import static com.destroystokyo.paper.MaterialTags.POTATOES;
import static com.destroystokyo.paper.MaterialTags.PRESSURE_PLATES;
import static com.destroystokyo.paper.MaterialTags.PRISMARINE;
import static com.destroystokyo.paper.MaterialTags.PRISMARINE_SLABS;
import static com.destroystokyo.paper.MaterialTags.PRISMARINE_STAIRS;
import static com.destroystokyo.paper.MaterialTags.PUMPKINS;
import static com.destroystokyo.paper.MaterialTags.QUARTZ_BLOCKS;
import static com.destroystokyo.paper.MaterialTags.RAILS;
import static com.destroystokyo.paper.MaterialTags.RAW_FISH;
import static com.destroystokyo.paper.MaterialTags.RAW_ORES;
import static com.destroystokyo.paper.MaterialTags.RAW_ORE_BLOCKS;
import static com.destroystokyo.paper.MaterialTags.REDSTONE_TORCH;
import static com.destroystokyo.paper.MaterialTags.RED_SANDSTONES;
import static com.destroystokyo.paper.MaterialTags.SANDSTONES;
import static com.destroystokyo.paper.MaterialTags.SHOVELS;
import static com.destroystokyo.paper.MaterialTags.SIGNS;
import static com.destroystokyo.paper.MaterialTags.SPAWN_EGGS;
import static com.destroystokyo.paper.MaterialTags.SPONGES;
import static com.destroystokyo.paper.MaterialTags.STAINED_GLASS;
import static com.destroystokyo.paper.MaterialTags.STAINED_GLASS_PANES;
import static com.destroystokyo.paper.MaterialTags.STAINED_TERRACOTTA;
import static com.destroystokyo.paper.MaterialTags.SWORDS;
import static com.destroystokyo.paper.MaterialTags.TERRACOTTA;
import static com.destroystokyo.paper.MaterialTags.TRAPDOORS;
import static com.destroystokyo.paper.MaterialTags.WOODEN_DOORS;
import static com.destroystokyo.paper.MaterialTags.WOODEN_FENCES;
import static com.destroystokyo.paper.MaterialTags.WOODEN_GATES;
import static org.bukkit.Material.ANCIENT_DEBRIS;
import static org.bukkit.Material.ANVIL;
import static org.bukkit.Material.APPLE;
import static org.bukkit.Material.ARMOR_STAND;
import static org.bukkit.Material.BAMBOO;
import static org.bukkit.Material.BARREL;
import static org.bukkit.Material.BARRIER;
import static org.bukkit.Material.BEACON;
import static org.bukkit.Material.BEDROCK;
import static org.bukkit.Material.BEEF;
import static org.bukkit.Material.BEEHIVE;
import static org.bukkit.Material.BEETROOT;
import static org.bukkit.Material.BEETROOT_SEEDS;
import static org.bukkit.Material.BEETROOT_SOUP;
import static org.bukkit.Material.BEE_NEST;
import static org.bukkit.Material.BELL;
import static org.bukkit.Material.BIG_DRIPLEAF;
import static org.bukkit.Material.BLAST_FURNACE;
import static org.bukkit.Material.BLAZE_POWDER;
import static org.bukkit.Material.BLAZE_ROD;
import static org.bukkit.Material.BONE;
import static org.bukkit.Material.BONE_BLOCK;
import static org.bukkit.Material.BONE_MEAL;
import static org.bukkit.Material.BOOKSHELF;
import static org.bukkit.Material.BOWL;
import static org.bukkit.Material.BREAD;
import static org.bukkit.Material.BREWING_STAND;
import static org.bukkit.Material.BRICK;
import static org.bukkit.Material.BRICKS;
import static org.bukkit.Material.BRICK_SLAB;
import static org.bukkit.Material.BRICK_STAIRS;
import static org.bukkit.Material.BRICK_WALL;
import static org.bukkit.Material.BUCKET;
import static org.bukkit.Material.BUNDLE;
import static org.bukkit.Material.CACTUS;
import static org.bukkit.Material.CAKE;
import static org.bukkit.Material.CALCITE;
import static org.bukkit.Material.CAMPFIRE;
import static org.bukkit.Material.CARROT;
import static org.bukkit.Material.CARROT_ON_A_STICK;
import static org.bukkit.Material.CARTOGRAPHY_TABLE;
import static org.bukkit.Material.CAULDRON;
import static org.bukkit.Material.CHAIN;
import static org.bukkit.Material.CHAIN_COMMAND_BLOCK;
import static org.bukkit.Material.CHARCOAL;
import static org.bukkit.Material.CHEST;
import static org.bukkit.Material.CHICKEN;
import static org.bukkit.Material.CHIPPED_ANVIL;
import static org.bukkit.Material.CHISELED_DEEPSLATE;
import static org.bukkit.Material.CLOCK;
import static org.bukkit.Material.COAL;
import static org.bukkit.Material.COAL_BLOCK;
import static org.bukkit.Material.COBWEB;
import static org.bukkit.Material.COCOA_BEANS;
import static org.bukkit.Material.COMMAND_BLOCK;
import static org.bukkit.Material.COMMAND_BLOCK_MINECART;
import static org.bukkit.Material.COMPARATOR;
import static org.bukkit.Material.COMPASS;
import static org.bukkit.Material.COMPOSTER;
import static org.bukkit.Material.COOKIE;
import static org.bukkit.Material.CRAFTING_TABLE;
import static org.bukkit.Material.CRYING_OBSIDIAN;
import static org.bukkit.Material.DAMAGED_ANVIL;
import static org.bukkit.Material.DAYLIGHT_DETECTOR;
import static org.bukkit.Material.DEAD_BUSH;
import static org.bukkit.Material.DEBUG_STICK;
import static org.bukkit.Material.DIAMOND;
import static org.bukkit.Material.DIAMOND_BLOCK;
import static org.bukkit.Material.DIRT_PATH;
import static org.bukkit.Material.DISPENSER;
import static org.bukkit.Material.DRIPSTONE_BLOCK;
import static org.bukkit.Material.DROPPER;
import static org.bukkit.Material.EGG;
import static org.bukkit.Material.EMERALD;
import static org.bukkit.Material.EMERALD_BLOCK;
import static org.bukkit.Material.ENCHANTING_TABLE;
import static org.bukkit.Material.ENDER_CHEST;
import static org.bukkit.Material.ENDER_EYE;
import static org.bukkit.Material.ENDER_PEARL;
import static org.bukkit.Material.END_PORTAL_FRAME;
import static org.bukkit.Material.END_ROD;
import static org.bukkit.Material.EXPERIENCE_BOTTLE;
import static org.bukkit.Material.FARMLAND;
import static org.bukkit.Material.FEATHER;
import static org.bukkit.Material.FERMENTED_SPIDER_EYE;
import static org.bukkit.Material.FERN;
import static org.bukkit.Material.FILLED_MAP;
import static org.bukkit.Material.FIRE_CHARGE;
import static org.bukkit.Material.FISHING_ROD;
import static org.bukkit.Material.FLETCHING_TABLE;
import static org.bukkit.Material.FLINT_AND_STEEL;
import static org.bukkit.Material.FLOWER_POT;
import static org.bukkit.Material.FURNACE;
import static org.bukkit.Material.GHAST_TEAR;
import static org.bukkit.Material.GLISTERING_MELON_SLICE;
import static org.bukkit.Material.GLOW_BERRIES;
import static org.bukkit.Material.GLOW_INK_SAC;
import static org.bukkit.Material.GLOW_ITEM_FRAME;
import static org.bukkit.Material.GLOW_LICHEN;
import static org.bukkit.Material.GOLDEN_CARROT;
import static org.bukkit.Material.GOLD_BLOCK;
import static org.bukkit.Material.GOLD_INGOT;
import static org.bukkit.Material.GOLD_NUGGET;
import static org.bukkit.Material.GRASS;
import static org.bukkit.Material.GRINDSTONE;
import static org.bukkit.Material.GUNPOWDER;
import static org.bukkit.Material.HANGING_ROOTS;
import static org.bukkit.Material.HAY_BLOCK;
import static org.bukkit.Material.HOPPER;
import static org.bukkit.Material.INK_SAC;
import static org.bukkit.Material.IRON_BARS;
import static org.bukkit.Material.IRON_BLOCK;
import static org.bukkit.Material.IRON_INGOT;
import static org.bukkit.Material.IRON_NUGGET;
import static org.bukkit.Material.ITEM_FRAME;
import static org.bukkit.Material.JIGSAW;
import static org.bukkit.Material.JUKEBOX;
import static org.bukkit.Material.KNOWLEDGE_BOOK;
import static org.bukkit.Material.LADDER;
import static org.bukkit.Material.LANTERN;
import static org.bukkit.Material.LAPIS_BLOCK;
import static org.bukkit.Material.LAPIS_LAZULI;
import static org.bukkit.Material.LARGE_FERN;
import static org.bukkit.Material.LAVA_BUCKET;
import static org.bukkit.Material.LEAD;
import static org.bukkit.Material.LEATHER;
import static org.bukkit.Material.LECTERN;
import static org.bukkit.Material.LEVER;
import static org.bukkit.Material.LIGHT;
import static org.bukkit.Material.LIGHTNING_ROD;
import static org.bukkit.Material.LILY_PAD;
import static org.bukkit.Material.LODESTONE;
import static org.bukkit.Material.LOOM;
import static org.bukkit.Material.MAGMA_CREAM;
import static org.bukkit.Material.MAP;
import static org.bukkit.Material.MELON;
import static org.bukkit.Material.MELON_SEEDS;
import static org.bukkit.Material.MELON_SLICE;
import static org.bukkit.Material.MILK_BUCKET;
import static org.bukkit.Material.MOSS_CARPET;
import static org.bukkit.Material.MUSHROOM_STEW;
import static org.bukkit.Material.MUTTON;
import static org.bukkit.Material.NAME_TAG;
import static org.bukkit.Material.NAUTILUS_SHELL;
import static org.bukkit.Material.NETHERITE_BLOCK;
import static org.bukkit.Material.NETHERITE_INGOT;
import static org.bukkit.Material.NETHERITE_SCRAP;
import static org.bukkit.Material.NETHER_STAR;
import static org.bukkit.Material.NOTE_BLOCK;
import static org.bukkit.Material.OBSERVER;
import static org.bukkit.Material.OBSIDIAN;
import static org.bukkit.Material.PAINTING;
import static org.bukkit.Material.PETRIFIED_OAK_SLAB;
import static org.bukkit.Material.PHANTOM_MEMBRANE;
import static org.bukkit.Material.POINTED_DRIPSTONE;
import static org.bukkit.Material.POPPED_CHORUS_FRUIT;
import static org.bukkit.Material.PORKCHOP;
import static org.bukkit.Material.POWDER_SNOW_BUCKET;
import static org.bukkit.Material.PUMPKIN_PIE;
import static org.bukkit.Material.PUMPKIN_SEEDS;
import static org.bukkit.Material.REDSTONE;
import static org.bukkit.Material.REDSTONE_BLOCK;
import static org.bukkit.Material.REDSTONE_LAMP;
import static org.bukkit.Material.REPEATER;
import static org.bukkit.Material.REPEATING_COMMAND_BLOCK;
import static org.bukkit.Material.RESPAWN_ANCHOR;
import static org.bukkit.Material.ROTTEN_FLESH;
import static org.bukkit.Material.SCAFFOLDING;
import static org.bukkit.Material.SCULK_SENSOR;
import static org.bukkit.Material.SCUTE;
import static org.bukkit.Material.SHEARS;
import static org.bukkit.Material.SHIELD;
import static org.bukkit.Material.SHULKER_SHELL;
import static org.bukkit.Material.SLIME_BALL;
import static org.bukkit.Material.SLIME_BLOCK;
import static org.bukkit.Material.SMALL_DRIPLEAF;
import static org.bukkit.Material.SMITHING_TABLE;
import static org.bukkit.Material.SMOKER;
import static org.bukkit.Material.SOUL_CAMPFIRE;
import static org.bukkit.Material.SOUL_LANTERN;
import static org.bukkit.Material.SOUL_TORCH;
import static org.bukkit.Material.SPAWNER;
import static org.bukkit.Material.SPIDER_EYE;
import static org.bukkit.Material.SPORE_BLOSSOM;
import static org.bukkit.Material.SPYGLASS;
import static org.bukkit.Material.STONE;
import static org.bukkit.Material.STONECUTTER;
import static org.bukkit.Material.STONE_BRICK_SLAB;
import static org.bukkit.Material.STONE_BRICK_STAIRS;
import static org.bukkit.Material.STONE_BRICK_WALL;
import static org.bukkit.Material.STONE_SLAB;
import static org.bukkit.Material.STONE_STAIRS;
import static org.bukkit.Material.STRING;
import static org.bukkit.Material.STRUCTURE_BLOCK;
import static org.bukkit.Material.STRUCTURE_VOID;
import static org.bukkit.Material.SUGAR;
import static org.bukkit.Material.SUGAR_CANE;
import static org.bukkit.Material.SUSPICIOUS_STEW;
import static org.bukkit.Material.SWEET_BERRIES;
import static org.bukkit.Material.TALL_GRASS;
import static org.bukkit.Material.TARGET;
import static org.bukkit.Material.TNT;
import static org.bukkit.Material.TORCH;
import static org.bukkit.Material.TOTEM_OF_UNDYING;
import static org.bukkit.Material.TRAPPED_CHEST;
import static org.bukkit.Material.TRIDENT;
import static org.bukkit.Material.TRIPWIRE_HOOK;
import static org.bukkit.Material.TUFF;
import static org.bukkit.Material.TURTLE_EGG;
import static org.bukkit.Material.VINE;
import static org.bukkit.Material.WATER_BUCKET;
import static org.bukkit.Material.WHEAT_SEEDS;
import static org.bukkit.Tag.BANNERS;
import static org.bukkit.Tag.BEDS;
import static org.bukkit.Tag.BUTTONS;
import static org.bukkit.Tag.CANDLES;
import static org.bukkit.Tag.CARPETS;
import static org.bukkit.Tag.CROPS;
import static org.bukkit.Tag.DIRT;
import static org.bukkit.Tag.FLOWERS;
import static org.bukkit.Tag.ICE;
import static org.bukkit.Tag.ITEMS_BOATS;
import static org.bukkit.Tag.ITEMS_FISHES;
import static org.bukkit.Tag.LEAVES;
import static org.bukkit.Tag.LOGS;
import static org.bukkit.Tag.LOGS_THAT_BURN;
import static org.bukkit.Tag.PLANKS;
import static org.bukkit.Tag.SAPLINGS;
import static org.bukkit.Tag.SHULKER_BOXES;
import static org.bukkit.Tag.STONE_BRICKS;
import static org.bukkit.Tag.WOODEN_BUTTONS;
import static org.bukkit.Tag.WOODEN_PRESSURE_PLATES;
import static org.bukkit.Tag.WOODEN_SLABS;
import static org.bukkit.Tag.WOODEN_STAIRS;
import static org.bukkit.Tag.WOODEN_TRAPDOORS;
import static org.bukkit.Tag.WOOL;

final class Categorizer {

    static boolean byTag(@NotNull BoxItem item, @NotNull Function<DefaultCategory, BoxCategory> func) {
        var category = checkTags(item.getOriginal());

        if (category != null) {
            func.apply(category).add(item);
            return true;
        } else {
            return false;
        }
    }

    private static @Nullable DefaultCategory checkTags(@NotNull ItemStack item) {
        // Check the tags in order of the amount of Material they have.

        var type = item.getType();

        if (SPAWN_EGGS.isTagged(type)) {
            return DefaultCategory.SPAWN_EGGS;
        }

        if (isTagged(item, SANDSTONES, STONE_BRICKS, RED_SANDSTONES)) {
            return DefaultCategory.STONES;
        }

        if (isTagged(item, MaterialTags.ORES, RAW_ORES, RAW_ORE_BLOCKS)) {
            return DefaultCategory.ORES;
        }

        if (DIRT.isTagged(type)) {
            return DefaultCategory.DIRT;
        }

        if (isTagged(item, TERRACOTTA, GLAZED_TERRACOTTA, STAINED_TERRACOTTA)) {
            return DefaultCategory.TERRACOTTA;
        }

        if (isTagged(item, CONCRETE_POWDER, CONCRETES)) {
            return DefaultCategory.CONCRETES;
        }

        if (isTagged(item, GLASS, GLASS_PANES, STAINED_GLASS, STAINED_GLASS_PANES)) {
            return DefaultCategory.GLASSES;
        }

        if (CANDLES.isTagged(type)) {
            return DefaultCategory.CANDLES;
        }

        if (DYES.isTagged(type)) {
            return DefaultCategory.DYES;
        }

        if (SHULKER_BOXES.isTagged(type)) {
            return DefaultCategory.SHULKER_BOXES;
        }

        if (isTagged(item, COOKED_FISH, GOLDEN_APPLES, POTATOES, PUMPKINS, ITEMS_FISHES, RAW_FISH, CROPS)) {
            return DefaultCategory.FARMS;
        }

        if (isTagged(
                item, CORAL, CORAL_BLOCKS, CORAL_FANS, FISH_BUCKETS, PRISMARINE, PRISMARINE_SLABS,
                PRISMARINE_STAIRS, SPONGES, ICE
        )) {
            return DefaultCategory.OCEANS;
        }

        if (isTagged(item, LOGS, LOGS_THAT_BURN, LEAVES, SAPLINGS, PLANKS, ITEMS_BOATS,
                WOODEN_BUTTONS, WOODEN_PRESSURE_PLATES, WOODEN_SLABS, WOODEN_STAIRS, WOODEN_FENCES,
                WOODEN_DOORS, WOODEN_GATES, WOODEN_TRAPDOORS, SIGNS)) {
            return DefaultCategory.WOODS;
        }

        if (isTagged(item, WOOL, CARPETS, BEDS, BANNERS)) {
            return DefaultCategory.WOOLS;
        }

        if (FLOWERS.isTagged(type)) {
            return DefaultCategory.FLOWERS;
        }

        if (isTagged(item, BOOTS, CHEST_EQUIPPABLE, CHESTPLATES, HEAD_EQUIPPABLE, HELMETS, LEGGINGS)) {
            return DefaultCategory.ARMORS;
        }

        if (isTagged(item, ARROWS, BOWS)) {
            return DefaultCategory.BOWS;
        }

        if (isTagged(item, AXES, HOES, PICKAXES, SHOVELS, SWORDS)) {
            return DefaultCategory.TOOLS;
        }

        if (isTagged(item, HORSE_ARMORS)) {
            return DefaultCategory.HORSE;
        }

        if (isTagged(item, MUSHROOM_BLOCKS, MUSHROOMS)) {
            return DefaultCategory.MUSHROOMS;
        }

        if (isTagged(item, BUTTONS, DOORS, FENCE_GATES, PISTONS, PRESSURE_PLATES, TRAPDOORS, REDSTONE_TORCH)) {
            return DefaultCategory.REDSTONES;
        }

        if (RAILS.isTagged(item)) {
            return DefaultCategory.RAILS;
        }

        if (QUARTZ_BLOCKS.isTagged(type)) {
            return DefaultCategory.NETHER;
        }

        if (MUSIC_DISCS.isTagged(type)) {
            return DefaultCategory.MUSIC_DISCS;
        }

        return null;
    }

    @SafeVarargs
    private static boolean isTagged(@NotNull ItemStack target, @NotNull Tag<Material> @NotNull ... materialTags) {
        for (var tag : materialTags) {
            if (tag.isTagged(target.getType())) {
                return true;
            }
        }

        return false;
    }

    static boolean byMaterial(@NotNull BoxItem item, @NotNull Function<DefaultCategory, BoxCategory> func) {
        var category = checkMaterial(item.getOriginal().getType());

        if (category != null) {
            func.apply(category).add(item);
            return true;
        } else {
            return false;
        }
    }

    private static final Set<Material> DECORATIONS = Set.of(
            ARMOR_STAND, ANVIL, BEACON, BELL, BLAST_FURNACE, BARREL, BOOKSHELF, BREWING_STAND, CAMPFIRE,
            CARTOGRAPHY_TABLE, CAULDRON, CHAIN, CHEST, CHIPPED_ANVIL, COMPOSTER, CRAFTING_TABLE, DAMAGED_ANVIL,
            ENCHANTING_TABLE, ENDER_CHEST, END_ROD, FLETCHING_TABLE, FURNACE, GLOW_ITEM_FRAME, GRINDSTONE,
            COBWEB, IRON_BARS, ITEM_FRAME, JUKEBOX, LANTERN, LECTERN, LIGHTNING_ROD, LODESTONE, LOOM,
            LADDER, PAINTING, RESPAWN_ANCHOR, SCAFFOLDING, SMITHING_TABLE, SMOKER,
            SOUL_CAMPFIRE, SOUL_LANTERN, SOUL_TORCH, STONECUTTER, TORCH
    );

    private static final Set<Material> FARMS = Set.of(
            APPLE, BEEF, BEEHIVE, BEE_NEST, BEETROOT, BEETROOT_SEEDS, BEETROOT_SOUP,
            BOWL, BREAD, CACTUS, CAKE, CARROT, CHICKEN, COOKIE, EGG, FARMLAND, FEATHER,
            GLISTERING_MELON_SLICE, GLOW_BERRIES, GLOW_LICHEN, GOLDEN_CARROT, COCOA_BEANS,
            HAY_BLOCK, LILY_PAD, MELON, MELON_SEEDS, MELON_SLICE, MOSS_CARPET, MUSHROOM_STEW,
            MUTTON, POPPED_CHORUS_FRUIT, PORKCHOP, PUMPKIN_PIE, PUMPKIN_SEEDS, SUGAR, SUGAR_CANE,
            SUSPICIOUS_STEW, SWEET_BERRIES, WHEAT_SEEDS
    );

    private static final Set<Material> FLOWER_ITEMS = Set.of(
            BAMBOO, BIG_DRIPLEAF, FLOWER_POT, DEAD_BUSH, FERN, GRASS, HANGING_ROOTS,
            LARGE_FERN, SMALL_DRIPLEAF, SPORE_BLOSSOM, TALL_GRASS, VINE
    );

    private static final Set<Material> MOB_DROPS = Set.of(
            BLAZE_POWDER, BLAZE_ROD, BONE, BONE_BLOCK, BONE_MEAL, ENDER_EYE, ENDER_PEARL,
            FERMENTED_SPIDER_EYE, FIRE_CHARGE, GHAST_TEAR, GLOW_INK_SAC, GUNPOWDER, INK_SAC,
            LEATHER, MAGMA_CREAM, NAUTILUS_SHELL, PHANTOM_MEMBRANE, NETHER_STAR, ROTTEN_FLESH,
            SCUTE, SHULKER_SHELL, SLIME_BALL, SLIME_BLOCK, SPIDER_EYE, STRING, TURTLE_EGG
    );

    private static final Set<Material> ORES = Set.of(
            COAL, COAL_BLOCK, CHARCOAL, DIAMOND, DIAMOND_BLOCK,
            LAPIS_LAZULI, LAPIS_BLOCK, EMERALD, EMERALD_BLOCK,
            IRON_INGOT, IRON_NUGGET, IRON_BLOCK,
            GOLD_INGOT, GOLD_NUGGET, GOLD_BLOCK,
            ANCIENT_DEBRIS, NETHERITE_INGOT, NETHERITE_SCRAP, NETHERITE_BLOCK,
            REDSTONE, REDSTONE_BLOCK
    );

    private static final Set<Material> REDSTONES = Set.of(
            COMPARATOR, DAYLIGHT_DETECTOR, DISPENSER, DROPPER, HOPPER, LEVER,
            NOTE_BLOCK, OBSERVER, REDSTONE_LAMP, REPEATER, SCULK_SENSOR,
            TARGET, TNT, TRAPPED_CHEST, TRIPWIRE_HOOK
    );

    private static final Set<Material> STONES = Set.of(
            BRICK, BRICKS, BRICK_SLAB, BRICK_STAIRS, BRICK_WALL, CALCITE, CHISELED_DEEPSLATE, CRYING_OBSIDIAN,
            DRIPSTONE_BLOCK, OBSIDIAN, POINTED_DRIPSTONE, STONE, STONE_BRICK_SLAB, STONE_BRICK_STAIRS,
            STONE_BRICK_WALL, STONE_SLAB, STONE_STAIRS, TUFF
    );

    private static final String[] STONE_FILTERS = {
            "ANDESITE", "BASALT", "BLACKSTONE", "COBBLESTONE", "DEEPSLATE",
            "DIORITE", "GRANITE", "SANDSTONE", "MOSSY_STONE", "SMOOTH_STONE"
    };

    private static final Set<Material> TOOLS = Set.of(
            BUCKET, BUNDLE, CARROT_ON_A_STICK, CLOCK, COMPASS, EXPERIENCE_BOTTLE,
            FISHING_ROD, FLINT_AND_STEEL, LAVA_BUCKET, LEAD, MAP, MILK_BUCKET,
            NAME_TAG, POWDER_SNOW_BUCKET, SHEARS, SHIELD, SPYGLASS, TOTEM_OF_UNDYING,
            TRIDENT, WATER_BUCKET
    );

    private static final Set<Material> UNAVAILABLE = Set.of(
            BARRIER, BEDROCK, CHAIN_COMMAND_BLOCK, COMMAND_BLOCK, COMMAND_BLOCK_MINECART,
            DEBUG_STICK, END_PORTAL_FRAME, FILLED_MAP, DIRT_PATH, JIGSAW,
            KNOWLEDGE_BOOK, LIGHT, PETRIFIED_OAK_SLAB, REPEATING_COMMAND_BLOCK,
            SPAWNER, STRUCTURE_BLOCK, STRUCTURE_VOID
    );

    private static @Nullable DefaultCategory checkMaterial(@NotNull Material type) {
        if (UNAVAILABLE.contains(type) || type.name().contains("INFESTED")) {
            return DefaultCategory.UNAVAILABLE;
        }

        if (DECORATIONS.contains(type)) {
            return DefaultCategory.DECORATIONS;
        }

        if (FARMS.contains(type) || contains(type, "COOKED", "HONEY", "RABBIT")) {
            return DefaultCategory.FARMS;
        }

        if (FLOWER_ITEMS.contains(type)) {
            return DefaultCategory.FLOWERS;
        }

        if (contains(type, "MINECART")) {
            return DefaultCategory.RAILS;
        }

        if (MOB_DROPS.contains(type)) {
            return DefaultCategory.MOB_DROPS;
        }

        if (ORES.contains(type) || contains(type, "AMETHYST", "COPPER")) {
            return DefaultCategory.ORES;
        }

        if (REDSTONES.contains(type)) {
            return DefaultCategory.REDSTONES;
        }

        if (TOOLS.contains(type)) {
            return DefaultCategory.TOOLS;
        }

        if (STONES.contains(type) || contains(type, STONE_FILTERS)) {
            return DefaultCategory.STONES;
        }

        if (type.name().contains("BANNER")) {
            return DefaultCategory.WOOLS;
        }

        if (contains(type, "CHORUS", "END_STONE", "PURPUR")) {
            return DefaultCategory.END;
        }

        if (contains(type, "NETHER", "CRIMSON", "QUARTZ", "WARPED")) {
            return DefaultCategory.NETHER;
        }

        if (contains(type, "PRISMARINE", "KELP", "SEA")) {
            return DefaultCategory.OCEANS;
        }

        return switch (type) {
            case CLAY, CLAY_BALL, GRAVEL, RED_SAND, SAND -> DefaultCategory.DIRT;
            case DRAGON_BREATH, DRAGON_EGG, END_CRYSTAL -> DefaultCategory.END;
            case ENCHANTED_BOOK -> DefaultCategory.ENCHANTED_BOOKS;
            case SADDLE -> DefaultCategory.HORSE;
            case AXOLOTL_BUCKET, CONDUIT, SNOW, SNOWBALL, SNOW_BLOCK -> DefaultCategory.OCEANS;
            case BOOK, FIREWORK_ROCKET, FIREWORK_STAR, FLINT,
                    PAPER, STICK, WRITABLE_BOOK, WRITTEN_BOOK -> DefaultCategory.MISC;
            case MUSHROOM_STEM -> DefaultCategory.MUSHROOMS;
            case GLOWSTONE, GLOWSTONE_DUST, MAGMA_BLOCK, SHROOMLIGHT,
                    SOUL_SAND, SOUL_SOIL, TWISTING_VINES, WEEPING_VINES -> DefaultCategory.NETHER;
            case GLASS_BOTTLE, POTION, SPLASH_POTION, LINGERING_POTION -> DefaultCategory.POTIONS;
            default -> null;
        };
    }

    private static boolean contains(@NotNull Material material, @NotNull String @NotNull ... names) {
        for (var name : names) {
            if (material.name().contains(name)) {
                return true;
            }
        }

        return false;
    }

    private Categorizer() {
        throw new UnsupportedOperationException();
    }
}
