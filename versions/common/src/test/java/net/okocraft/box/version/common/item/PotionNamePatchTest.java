package net.okocraft.box.version.common.item;

import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

class PotionNamePatchTest {

    @Test
    void test() {
        var legacyPotions = legacyPotions();
        var patchedPotions = new ArrayList<String>(legacyPotions.size());

        for (var potion : legacyPotions()) {
            var patched = LegacyVersionPatches.potionName(potion);
            var potionName = stripMaterialName(patched);
            if (potion.endsWith("_EXTENDED") || potion.endsWith("_UPGRADED")) {
                Assertions.assertDoesNotThrow(() -> PotionType.valueOf(potionName));
            } else {
                Assertions.assertSame(potion, LegacyVersionPatches.potionName(potion));
            }

            if (potionName != null) {
                patchedPotions.add(patched);
            }
        }

        var allPotionItems = new HashSet<String>();

        for (var type :  PotionType.values()) {
            if (type != PotionType.UNCRAFTABLE) {
                allPotionItems.add("POTION_" + type.name());
                allPotionItems.add("SPLASH_POTION_" + type.name());
                allPotionItems.add("LINGERING_POTION_" + type.name());
                allPotionItems.add("TIPPED_ARROW_" + type.name());
            }
        }

        for (var patchedPotion : patchedPotions) {
            Assertions.assertTrue(allPotionItems.remove(patchedPotion));
        }

        Assertions.assertTrue(allPotionItems.isEmpty());
    }

    private static String stripMaterialName(String original) {
        if (original.startsWith("POTION_")) {
            return original.substring(7);
        } else if (original.startsWith("SPLASH_POTION_")) {
            return original.substring(14);
        } else if (original.startsWith("LINGERING_POTION_")) {
            return original.substring(17);
        } else if (original.startsWith("TIPPED_ARROW_")) {
            return original.substring(13);
        } else {
            return null;
        }
    }

    private static @NotNull Collection<String> legacyPotions() {
        return ObjectImmutableList.of(
                "POTION",
                "POTION_WATER",
                "POTION_AWKWARD",
                "POTION_MUNDANE",
                "POTION_THICK",
                "POTION_FIRE_RESISTANCE",
                "POTION_FIRE_RESISTANCE_EXTENDED",
                "POTION_INSTANT_DAMAGE",
                "POTION_INSTANT_DAMAGE_UPGRADED",
                "POTION_INSTANT_HEAL",
                "POTION_INSTANT_HEAL_UPGRADED",
                "POTION_INVISIBILITY",
                "POTION_INVISIBILITY_EXTENDED",
                "POTION_JUMP",
                "POTION_JUMP_EXTENDED",
                "POTION_JUMP_UPGRADED",
                "POTION_LUCK",
                "POTION_NIGHT_VISION",
                "POTION_NIGHT_VISION_EXTENDED",
                "POTION_POISON",
                "POTION_POISON_EXTENDED",
                "POTION_POISON_UPGRADED",
                "POTION_REGEN",
                "POTION_REGEN_EXTENDED",
                "POTION_REGEN_UPGRADED",
                "POTION_SLOWNESS",
                "POTION_SLOWNESS_EXTENDED",
                "POTION_SLOWNESS_UPGRADED",
                "POTION_SLOW_FALLING",
                "POTION_SLOW_FALLING_EXTENDED",
                "POTION_SPEED",
                "POTION_SPEED_EXTENDED",
                "POTION_SPEED_UPGRADED",
                "POTION_STRENGTH",
                "POTION_STRENGTH_EXTENDED",
                "POTION_STRENGTH_UPGRADED",
                "POTION_TURTLE_MASTER",
                "POTION_TURTLE_MASTER_EXTENDED",
                "POTION_TURTLE_MASTER_UPGRADED",
                "POTION_WATER_BREATHING",
                "POTION_WATER_BREATHING_EXTENDED",
                "POTION_WEAKNESS",
                "POTION_WEAKNESS_EXTENDED",
                "SPLASH_POTION_WATER",
                "SPLASH_POTION_AWKWARD",
                "SPLASH_POTION_MUNDANE",
                "SPLASH_POTION_THICK",
                "SPLASH_POTION_FIRE_RESISTANCE",
                "SPLASH_POTION_FIRE_RESISTANCE_EXTENDED",
                "SPLASH_POTION_INSTANT_DAMAGE",
                "SPLASH_POTION_INSTANT_DAMAGE_UPGRADED",
                "SPLASH_POTION_INSTANT_HEAL",
                "SPLASH_POTION_INSTANT_HEAL_UPGRADED",
                "SPLASH_POTION_INVISIBILITY",
                "SPLASH_POTION_INVISIBILITY_EXTENDED",
                "SPLASH_POTION_JUMP",
                "SPLASH_POTION_JUMP_EXTENDED",
                "SPLASH_POTION_JUMP_UPGRADED",
                "SPLASH_POTION_LUCK",
                "SPLASH_POTION_NIGHT_VISION",
                "SPLASH_POTION_NIGHT_VISION_EXTENDED",
                "SPLASH_POTION_POISON",
                "SPLASH_POTION_POISON_EXTENDED",
                "SPLASH_POTION_POISON_UPGRADED",
                "SPLASH_POTION_REGEN",
                "SPLASH_POTION_REGEN_EXTENDED",
                "SPLASH_POTION_REGEN_UPGRADED",
                "SPLASH_POTION_SLOWNESS",
                "SPLASH_POTION_SLOWNESS_EXTENDED",
                "SPLASH_POTION_SLOWNESS_UPGRADED",
                "SPLASH_POTION_SLOW_FALLING",
                "SPLASH_POTION_SLOW_FALLING_EXTENDED",
                "SPLASH_POTION_SPEED",
                "SPLASH_POTION_SPEED_EXTENDED",
                "SPLASH_POTION_SPEED_UPGRADED",
                "SPLASH_POTION_STRENGTH",
                "SPLASH_POTION_STRENGTH_EXTENDED",
                "SPLASH_POTION_STRENGTH_UPGRADED",
                "SPLASH_POTION_TURTLE_MASTER",
                "SPLASH_POTION_TURTLE_MASTER_EXTENDED",
                "SPLASH_POTION_TURTLE_MASTER_UPGRADED",
                "SPLASH_POTION_WATER_BREATHING",
                "SPLASH_POTION_WATER_BREATHING_EXTENDED",
                "SPLASH_POTION_WEAKNESS",
                "SPLASH_POTION_WEAKNESS_EXTENDED",
                "LINGERING_POTION_AWKWARD",
                "LINGERING_POTION_MUNDANE",
                "LINGERING_POTION_THICK",
                "LINGERING_POTION_WATER",
                "LINGERING_POTION_FIRE_RESISTANCE",
                "LINGERING_POTION_FIRE_RESISTANCE_EXTENDED",
                "LINGERING_POTION_INSTANT_DAMAGE",
                "LINGERING_POTION_INSTANT_DAMAGE_UPGRADED",
                "LINGERING_POTION_INSTANT_HEAL",
                "LINGERING_POTION_INSTANT_HEAL_UPGRADED",
                "LINGERING_POTION_INVISIBILITY",
                "LINGERING_POTION_INVISIBILITY_EXTENDED",
                "LINGERING_POTION_JUMP",
                "LINGERING_POTION_JUMP_EXTENDED",
                "LINGERING_POTION_JUMP_UPGRADED",
                "LINGERING_POTION_LUCK",
                "LINGERING_POTION_NIGHT_VISION",
                "LINGERING_POTION_NIGHT_VISION_EXTENDED",
                "LINGERING_POTION_POISON",
                "LINGERING_POTION_POISON_EXTENDED",
                "LINGERING_POTION_POISON_UPGRADED",
                "LINGERING_POTION_REGEN",
                "LINGERING_POTION_REGEN_EXTENDED",
                "LINGERING_POTION_REGEN_UPGRADED",
                "LINGERING_POTION_SLOWNESS",
                "LINGERING_POTION_SLOWNESS_EXTENDED",
                "LINGERING_POTION_SLOWNESS_UPGRADED",
                "LINGERING_POTION_SLOW_FALLING",
                "LINGERING_POTION_SLOW_FALLING_EXTENDED",
                "LINGERING_POTION_SPEED",
                "LINGERING_POTION_SPEED_EXTENDED",
                "LINGERING_POTION_SPEED_UPGRADED",
                "LINGERING_POTION_STRENGTH",
                "LINGERING_POTION_STRENGTH_EXTENDED",
                "LINGERING_POTION_STRENGTH_UPGRADED",
                "LINGERING_POTION_TURTLE_MASTER",
                "LINGERING_POTION_TURTLE_MASTER_EXTENDED",
                "LINGERING_POTION_TURTLE_MASTER_UPGRADED",
                "LINGERING_POTION_WATER_BREATHING",
                "LINGERING_POTION_WATER_BREATHING_EXTENDED",
                "LINGERING_POTION_WEAKNESS",
                "LINGERING_POTION_WEAKNESS_EXTENDED",
                "TIPPED_ARROW_AWKWARD",
                "TIPPED_ARROW_FIRE_RESISTANCE",
                "TIPPED_ARROW_FIRE_RESISTANCE_EXTENDED",
                "TIPPED_ARROW_INSTANT_DAMAGE",
                "TIPPED_ARROW_INSTANT_DAMAGE_UPGRADED",
                "TIPPED_ARROW_INSTANT_HEAL",
                "TIPPED_ARROW_INSTANT_HEAL_UPGRADED",
                "TIPPED_ARROW_INVISIBILITY",
                "TIPPED_ARROW_INVISIBILITY_EXTENDED",
                "TIPPED_ARROW_JUMP",
                "TIPPED_ARROW_JUMP_EXTENDED",
                "TIPPED_ARROW_JUMP_UPGRADED",
                "TIPPED_ARROW_LUCK",
                "TIPPED_ARROW_MUNDANE",
                "TIPPED_ARROW_NIGHT_VISION",
                "TIPPED_ARROW_NIGHT_VISION_EXTENDED",
                "TIPPED_ARROW_POISON",
                "TIPPED_ARROW_POISON_EXTENDED",
                "TIPPED_ARROW_POISON_UPGRADED",
                "TIPPED_ARROW_REGEN",
                "TIPPED_ARROW_REGEN_EXTENDED",
                "TIPPED_ARROW_REGEN_UPGRADED",
                "TIPPED_ARROW_SLOWNESS",
                "TIPPED_ARROW_SLOWNESS_EXTENDED",
                "TIPPED_ARROW_SLOWNESS_UPGRADED",
                "TIPPED_ARROW_SLOW_FALLING",
                "TIPPED_ARROW_SLOW_FALLING_EXTENDED",
                "TIPPED_ARROW_SPEED",
                "TIPPED_ARROW_SPEED_EXTENDED",
                "TIPPED_ARROW_SPEED_UPGRADED",
                "TIPPED_ARROW_STRENGTH",
                "TIPPED_ARROW_STRENGTH_EXTENDED",
                "TIPPED_ARROW_STRENGTH_UPGRADED",
                "TIPPED_ARROW_THICK",
                "TIPPED_ARROW_TURTLE_MASTER",
                "TIPPED_ARROW_TURTLE_MASTER_EXTENDED",
                "TIPPED_ARROW_TURTLE_MASTER_UPGRADED",
                "TIPPED_ARROW_WATER",
                "TIPPED_ARROW_WATER_BREATHING",
                "TIPPED_ARROW_WATER_BREATHING_EXTENDED",
                "TIPPED_ARROW_WEAKNESS",
                "TIPPED_ARROW_WEAKNESS_EXTENDED"
        );
    }
}
