package net.okocraft.box.feature.craft.loader;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class AdditionalRecipes {

    static @NotNull List<Recipe> getFireworkRocketRecipes() {
        return List.of(fireworkRocket(2), fireworkRocket(3));
    }

    private static @NotNull Recipe fireworkRocket(int power) {
        var firework = new ItemStack(Material.FIREWORK_ROCKET, 3);

        if (firework.getItemMeta() instanceof FireworkMeta meta) {
            meta.setPower(power);
            firework.setItemMeta(meta);
        }

        return new ShapelessRecipe(createFireworkRecipeKey(power), firework)
                .addIngredient(power, Material.GUNPOWDER)
                .addIngredient(1, Material.PAPER);
    }

    private static @NotNull NamespacedKey createFireworkRecipeKey(int power) {
        return new NamespacedKey("box", "recipe_firework_rocket_" + power);
    }
}
