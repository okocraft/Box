package net.okocraft.box.feature.category.impl;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public enum DefaultCategory {
    ARMORS("armors", Material.LEATHER_HELMET),
    BOWS("bows", Material.BOW),
    CANDLES("candles", Material.CANDLE),
    CONCRETES("concretes", Material.WHITE_CONCRETE),
    CUSTOM_ITEMS("custom-items", Material.NETHER_STAR),
    DECORATIONS("decorations", Material.CRAFTING_TABLE),
    DIRT("dirt", Material.DIRT),
    DYES("dyes", Material.RED_DYE),
    ENCHANTED_BOOKS("enchanted-books", Material.ENCHANTED_BOOK),
    END("end", Material.END_STONE),
    FARMS("farms", Material.WHEAT),
    FLOWERS("flowers", Material.POPPY),
    GLASSES("glasses", Material.GLASS),
    HORSE("horse", Material.SADDLE),
    MISC("misc", Material.PAPER),
    MOB_DROPS("mob-drops", Material.ROTTEN_FLESH),
    MUSHROOMS("mushrooms", Material.RED_MUSHROOM),
    MUSIC_DISCS("music-discs", Material.MUSIC_DISC_CAT),
    NETHER("nether", Material.NETHERRACK),
    OCEANS("oceans", Material.KELP),
    ORES("ores", Material.IRON_ORE),
    POTIONS("potions", Material.POTION),
    RAILS("rails", Material.RAIL),
    REDSTONES("redstones", Material.REDSTONE),
    SHULKER_BOXES("shulker-boxes", Material.SHULKER_BOX),
    SPAWN_EGGS("spawn-eggs", Material.COW_SPAWN_EGG),
    STONES("stones", Material.STONE),
    TERRACOTTA("terracotta", Material.TERRACOTTA),
    TOOLS("tools", Material.IRON_PICKAXE),
    UNAVAILABLE("unavailable", Material.BEDROCK),
    UNCATEGORIZED("uncategorized", Material.KNOWLEDGE_BOOK),
    WOODS("woods", Material.OAK_LOG),
    WOOLS("wools", Material.WHITE_WOOL),
    ;

    private final String name;
    private final Material iconMaterial;

    DefaultCategory(@NotNull String name, @NotNull Material iconMaterial) {
        this.name = name;
        this.iconMaterial = iconMaterial;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Material getIconMaterial() {
        return iconMaterial;
    }
}
