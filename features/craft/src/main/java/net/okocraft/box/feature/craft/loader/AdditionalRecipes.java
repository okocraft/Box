package net.okocraft.box.feature.craft.loader;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.craft.model.BoxIngredientItem;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

final class AdditionalRecipes {

    static void addFireworkRocketRecipes(@NotNull Processor processor) {
        var itemManager = BoxAPI.api().getItemManager();

        var paper = itemManager.getBoxItem("PAPER").orElse(null);
        var gunpowder = itemManager.getBoxItem("GUNPOWDER").orElse(null);

        if (paper == null || gunpowder == null) {
            BoxLogger.logger().warn("Cannot create firework rocket recipes because PAPER or GUNPOWDER is not found");
            return;
        }

        var gunpowderIngredientItems = List.of(new BoxIngredientItem(gunpowder, 1));
        var paperIngredientItems = List.of(new BoxIngredientItem(paper, 1));

        IntStream.of(2, 3).forEach(power -> {
            var firework = itemManager.getBoxItem(ItemType.FIREWORK_ROCKET.createItemStack(1, meta -> meta.setPower(power))).orElse(null);
            if (firework == null) {
                BoxLogger.logger().warn("Cannot create firework rocket recipes because firework (power {}) is not found", power);
                return;
            }

            var ingredients = new ArrayList<IngredientHolder>(power + 1);
            for (int i = 0; i < power; i++) {
                ingredients.add(IngredientHolder.create(i, gunpowderIngredientItems));
            }
            ingredients.add(IngredientHolder.create(power, paperIngredientItems));

            processor.addRecipe(ingredients, firework, 3);
        });
    }
}
