package net.okocraft.box.feature.craft.loader;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.craft.config.RecipeConfig;
import net.okocraft.box.feature.craft.event.RecipeImportEvent;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Processor {

    private final RecipeConfig recipeConfig;
    private final ItemManager itemManager = BoxAPI.api().getItemManager();
    private final Map<BoxItem, RecipeHolder> recipeMap = new HashMap<>(100, 0.8f);

    Processor(@NotNull RecipeConfig recipeConfig) {
        this.recipeConfig = recipeConfig;
    }

    void processRecipe(@NotNull Recipe recipe) {
        var result = this.itemManager.getBoxItem(recipe.getResult());

        if (result.isEmpty()) {
            return;
        }

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            this.processShapedRecipe(shapedRecipe, result.get());
            return;
        }

        if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            this.processShapelessRecipe(shapelessRecipe, result.get());
        }
    }

    void processCustomRecipes() {
        var logger = BoxLogger.logger();
        var itemManager = BoxAPI.api().getItemManager();

        for (var customRecipe : this.recipeConfig.customRecipes()) {
            var resultItem = itemManager.getBoxItem(customRecipe.result());

            if (resultItem.isEmpty()) {
                logger.warn("Could not get a result item in recipes.yml ({})", customRecipe.result());
                continue;
            }

            var ingredientsNames = customRecipe.ingredients();

            if (ingredientsNames.isEmpty()) {
                logger.warn("No ingredients specified for {}", customRecipe.result());
                continue;
            }

            if (9 < ingredientsNames.size()) {
                logger.warn("Too many ingredients for {}", customRecipe.result());
                continue;
            }

            var ingredients = new BoxItem[ingredientsNames.size()];
            boolean process = false; // Indicates whether there is at least one or more valid ingredient and there is no unknown ingredient.

            for (int i = 0, s = ingredientsNames.size(); i < s; i++) {
                var ingredientName = ingredientsNames.get(i);

                if (ingredientName.equalsIgnoreCase("air")) {
                    continue;
                }

                var ingredient = itemManager.getBoxItem(ingredientName);

                if (ingredient.isEmpty()) {
                    logger.warn("Could not get an ingredient item in recipes.yml ({})", ingredientName);
                    process = false;
                    break;
                } else {
                    ingredients[i] = ingredient.get();
                    process = true;
                }
            }

            if (process) {
                this.processCustomRecipe(ingredients, resultItem.get(), Math.max(1, customRecipe.amount()));
            }
        }
    }

    @NotNull Map<BoxItem, RecipeHolder> result() {
        return this.recipeMap;
    }

    private void processShapedRecipe(@NotNull ShapedRecipe recipe, @NotNull BoxItem result) {
        if (this.recipeConfig.disabledRecipes().contains(recipe.getKey().toString())) {
            return;
        }

        var ingredients = new ArrayList<IngredientHolder>();

        for (var entry : recipe.getChoiceMap().entrySet()) {
            var slot = this.getPosition(entry.getKey(), recipe.getShape());
            var choice = entry.getValue();

            if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
                ingredients.add(IngredientHolder.fromMaterialChoice(slot, materialChoice));
            } else if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
                var ingredientItem = IngredientHolder.fromExactChoice(slot, exactChoice);

                if (ingredientItem.patterns().isEmpty()) {
                    return;
                } else {
                    ingredients.add(IngredientHolder.fromExactChoice(slot, exactChoice));
                }
            } else if (choice != null) {
                return;
            }
        }

        this.addRecipe(ingredients, result, recipe.getResult().getAmount());
    }

    private void processShapelessRecipe(@NotNull ShapelessRecipe recipe, @NotNull BoxItem result) {
        if (this.recipeConfig.disabledRecipes().contains(recipe.getKey().toString())) {
            return;
        }

        var ingredients = new ArrayList<IngredientHolder>();

        int slot = 0;
        for (var choice : recipe.getChoiceList()) {
            if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
                ingredients.add(IngredientHolder.fromMaterialChoice(slot, materialChoice));
            } else if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
                var ingredientItem = IngredientHolder.fromExactChoice(slot, exactChoice);

                if (ingredientItem.patterns().isEmpty()) {
                    return;
                } else {
                    ingredients.add(IngredientHolder.fromExactChoice(slot, exactChoice));
                }
            } else {
                return;
            }

            slot++;
        }

        this.addRecipe(ingredients, result, recipe.getResult().getAmount());
    }

    private void processCustomRecipe(@Nullable BoxItem @NotNull [] ingredients, @NotNull BoxItem result, int amount) {
        var ingredientHolders = new ArrayList<IngredientHolder>();

        for (int i = 0; i < ingredients.length; i++) {
            var ingredient = ingredients[i];
            if (ingredient != null) {
                ingredientHolders.add(IngredientHolder.fromSingleItem(i, ingredient.getOriginal()));
            }
        }

        this.addRecipe(ingredientHolders, result, amount);
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

    void addRecipe(@NotNull List<IngredientHolder> ingredients, @NotNull BoxItem result, int amount) {
        var recipe = new BoxItemRecipe(ingredients, result, amount);
        this.recipeMap.computeIfAbsent(result, i -> new RecipeHolder()).addRecipe(recipe);
        BoxAPI.api().getEventCallers().sync().call(new RecipeImportEvent(recipe));
    }
}
