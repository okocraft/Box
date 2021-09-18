package net.okocraft.box.feature.craft.loader;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

final class Processor {

    private final ItemManager itemManager = BoxProvider.get().getItemManager();
    private final Map<BoxItem, RecipeHolder> recipeMap = new HashMap<>();

    void processRecipe(@NotNull Recipe recipe) {
        var result = itemManager.getBoxItem(recipe.getResult());

        if (result.isEmpty()) {
            return;
        }

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            processShapedRecipe(shapedRecipe, result.get());
            return;
        }

        if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            processShapelessRecipe(shapelessRecipe, result.get());
        }
    }

    @NotNull Map<BoxItem, RecipeHolder> result() {
        return recipeMap;
    }

    private void processShapedRecipe(@NotNull ShapedRecipe recipe, @NotNull BoxItem result) {
        var ingredients = new ArrayList<IngredientHolder>();

        for (var entry : recipe.getChoiceMap().entrySet()) {
            var slot = getPosition(entry.getKey(), recipe.getShape());
            var choice = entry.getValue();

            if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
                ingredients.add(IngredientHolder.fromMaterialChoice(slot, materialChoice));
            } else if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
                var ingredientItem = IngredientHolder.fromExactChoice(slot, exactChoice);

                if (ingredientItem.getPatterns().isEmpty()) {
                    return;
                } else {
                    ingredients.add(IngredientHolder.fromExactChoice(slot, exactChoice));
                }
            } else if (choice != null) {
                return;
            }
        }

        getRecipeHolder(result).addRecipe(new BoxItemRecipe(ingredients, result, recipe.getResult().getAmount()));
    }

    private void processShapelessRecipe(@NotNull ShapelessRecipe recipe, @NotNull BoxItem result) {
        var ingredients = new ArrayList<IngredientHolder>();

        int slot = 0;
        for (var choice : recipe.getChoiceList()) {
            if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
                ingredients.add(IngredientHolder.fromMaterialChoice(slot, materialChoice));
            } else if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
                var ingredientItem = IngredientHolder.fromExactChoice(slot, exactChoice);

                if (ingredientItem.getPatterns().isEmpty()) {
                    return;
                } else {
                    ingredients.add(IngredientHolder.fromExactChoice(slot, exactChoice));
                }
            } else {
                return;
            }

            slot++;
        }

        getRecipeHolder(result).addRecipe(new BoxItemRecipe(ingredients, result, recipe.getResult().getAmount()));

    }

    @Contract(pure = true)
    private int getPosition(char c, String @NotNull [] shape) {
        for (int row = 0; row < shape.length; row++) {
            var str = shape[row];
            var pos = str.indexOf(c);

            if (pos != -1) {
                return pos + (row * 3);
            }
        }

        return 8;
    }

    private @NotNull RecipeHolder getRecipeHolder(@NotNull BoxItem item) {
        return recipeMap.computeIfAbsent(item, i -> new RecipeHolder());
    }
}
