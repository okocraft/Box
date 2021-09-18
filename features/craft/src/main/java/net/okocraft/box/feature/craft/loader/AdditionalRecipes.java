package net.okocraft.box.feature.craft.loader;

import net.okocraft.box.api.BoxProvider;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class AdditionalRecipes {

    static final List<Recipe> RECIPES;

    static {
        RECIPES = List.of(
                fireworkRocket(1), fireworkRocket(2), fireworkRocket(3)
        );
    }

    private static @NotNull Recipe fireworkRocket(int power) {
        var namespace = BoxProvider.get().createNamespacedKey("recipe_firework_rocket_1");
        var firework = new ItemStack(Material.FIREWORK_ROCKET);

        if (firework.getItemMeta() instanceof FireworkMeta meta) {
            meta.setPower(power);
            firework.setItemMeta(meta);
        }

        return new ShapelessRecipe(namespace, firework)
                .addIngredient(power, Material.GUNPOWDER)
                .addIngredient(1, Material.PAPER);
    }
}
