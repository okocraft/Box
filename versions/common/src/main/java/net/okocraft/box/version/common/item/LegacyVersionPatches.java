package net.okocraft.box.version.common.item;

import net.okocraft.box.api.model.item.ItemVersion;
import net.okocraft.box.storage.api.model.item.ItemData;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.jetbrains.annotations.NotNull;

import static net.okocraft.box.api.util.MCDataVersion.MC_1_19;
import static net.okocraft.box.api.util.MCDataVersion.MC_1_19_4;
import static net.okocraft.box.api.util.MCDataVersion.MC_1_20_3;
import static net.okocraft.box.api.util.MCDataVersion.MC_1_20_5;

public final class LegacyVersionPatches {

    public static boolean shouldPatchGoatHorn(@NotNull ItemVersion version) {
        return version.dataVersion().isBetween(MC_1_19, MC_1_19_4) && version.defaultItemVersion() == 0;
    }

    public static @NotNull String goatHornName(@NotNull String original) {
        return original.equals("GOAT_HORN") ? "PONDER_GOAT_HORN" : original;
    }

    public static @NotNull ItemData goatHorn(@NotNull ItemData itemData) {
        if (itemData.plainName().equals("GOAT_HORN")) {
            var goatHorn = new ItemStack(Material.GOAT_HORN);
            goatHorn.editMeta(MusicInstrumentMeta.class, meta -> meta.setInstrument(MusicInstrument.PONDER_GOAT_HORN));

            return new ItemData(itemData.internalId(), "PONDER_GOAT_HORN", goatHorn.serializeAsBytes());
        } else {
            return itemData;
        }
    }

    public static boolean shouldPatchShortGrassName(@NotNull ItemVersion starting, @NotNull ItemVersion current) {
        return starting.dataVersion().isBefore(MC_1_20_3) && current.dataVersion().isAfterOrSame(MC_1_20_3);
    }

    public static @NotNull String shortGrassName(@NotNull String original) {
        return original.equals("GRASS") ? "SHORT_GRASS" : original;
    }

    public static boolean shouldPatchPotionName(@NotNull ItemVersion starting, @NotNull ItemVersion current) {
        return starting.dataVersion().isBefore(MC_1_20_5) && current.dataVersion().isAfterOrSame(MC_1_20_5);
    }

    public static @NotNull String potionName(@NotNull String original) {
        String material;
        String potionType;

        if (original.startsWith("POTION_")) {
            material = "POTION_";
            potionType = original.substring(7);
        } else if (original.startsWith("SPLASH_POTION_")) {
            material = "SPLASH_POTION_";
            potionType = original.substring(14);
        } else if (original.startsWith("LINGERING_POTION_")) {
            material = "LINGERING_POTION_";
            potionType = original.substring(17);
        } else if (original.startsWith("TIPPED_ARROW_")) {
            material = "TIPPED_ARROW_";
            potionType = original.substring(13);
        } else {
            return original;
        }

        if (original.endsWith("_EXTENDED")) {
            return material + switch (potionType) {
                case "FIRE_RESISTANCE_EXTENDED" -> "LONG_FIRE_RESISTANCE";
                case "INVISIBILITY_EXTENDED" -> "LONG_INVISIBILITY";
                case "JUMP_EXTENDED" -> "LONG_LEAPING";
                case "NIGHT_VISION_EXTENDED" -> "LONG_NIGHT_VISION";
                case "POISON_EXTENDED" -> "LONG_POISON";
                case "REGEN_EXTENDED" -> "LONG_REGENERATION";
                case "SLOW_FALLING_EXTENDED" -> "LONG_SLOW_FALLING";
                case "SLOWNESS_EXTENDED" -> "LONG_SLOWNESS";
                case "STRENGTH_EXTENDED" -> "LONG_STRENGTH";
                case "SPEED_EXTENDED" -> "LONG_SWIFTNESS";
                case "TURTLE_MASTER_EXTENDED" -> "LONG_TURTLE_MASTER";
                case "WATER_BREATHING_EXTENDED" -> "LONG_WATER_BREATHING";
                case "WEAKNESS_EXTENDED" -> "LONG_WEAKNESS";
                default -> throw new IllegalArgumentException("Unknown extended potion: " + original);
            };
        } else if (original.endsWith("_UPGRADED")){
            return material + switch (potionType) {
                case "INSTANT_DAMAGE_UPGRADED" -> "STRONG_HARMING";
                case "INSTANT_HEAL_UPGRADED" -> "STRONG_HEALING";
                case "JUMP_UPGRADED" -> "STRONG_LEAPING";
                case "POISON_UPGRADED" -> "STRONG_POISON";
                case "REGEN_UPGRADED" -> "STRONG_REGENERATION";
                case "SLOWNESS_UPGRADED" -> "STRONG_SLOWNESS";
                case "STRENGTH_UPGRADED" -> "STRONG_STRENGTH";
                case "SPEED_UPGRADED" -> "STRONG_SWIFTNESS";
                case "TURTLE_MASTER_UPGRADED" -> "STRONG_TURTLE_MASTER";
                default -> throw new IllegalArgumentException("Unknown upgraded potion: " + original);
            };
        } else {
            return material + switch (potionType) {
                case "JUMP" -> "LEAPING";
                case "SPEED" -> "SWIFTNESS";
                case "INSTANT_HEAL" -> "HEALING";
                case "INSTANT_DAMAGE" -> "HARMING";
                case "REGEN" -> "REGENERATION";
                default -> potionType;
            };
        }
    }

    public static boolean shouldPatchTurtleScuteName(@NotNull ItemVersion starting, @NotNull ItemVersion current) {
        return starting.dataVersion().isBefore(MC_1_20_5) && current.dataVersion().isAfterOrSame(MC_1_20_5);
    }

    public static @NotNull String turtleScute(@NotNull String original) {
        return original.equals("SCUTE") ? "TURTLE_SCUTE" : original;
    }

    private LegacyVersionPatches() {
        throw new UnsupportedOperationException();
    }

}
