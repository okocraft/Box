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

    public static @NotNull String shortGrassName(@NotNull String original) {
        return original.equals("GRASS") ? "SHORT_GRASS" : original;
    }

    public static @NotNull String potionName(@NotNull String original) {
        boolean extended = original.endsWith("_EXTENDED");
        boolean upgraded = original.endsWith("_UPGRADED");

        if (!extended && !upgraded) {
            return original;
        }

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

        if (extended) {
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
        } else { // upgraded
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
        }
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
