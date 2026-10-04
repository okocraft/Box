package net.okocraft.box.feature.craft.loader;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.craft.config.CustomRecipe;
import net.okocraft.box.feature.craft.config.RecipeConfig;
import net.okocraft.box.feature.craft.event.RecipeImportEvent;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class Processor {

    private final RecipeConfig recipeConfig;
    private final ItemManager itemManager = BoxAPI.api().getItemManager();
    private final Map<BoxItem, RecipeHolder> recipeMap = new HashMap<>(100, 0.8f);

    Processor(@NotNull RecipeConfig recipeConfig) {
        this.recipeConfig = recipeConfig;
    }

    void processRecipe(@NotNull Recipe recipe) {
        Optional<BoxItem> result = this.itemManager.getBoxItem(recipe.getResult());

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
        Logger logger = BoxLogger.logger();
        ItemManager itemManager = BoxAPI.api().getItemManager();

        for (CustomRecipe customRecipe : this.recipeConfig.customRecipes()) {
            Optional<BoxItem> resultItem = itemManager.getBoxItem(customRecipe.result());

            if (resultItem.isEmpty()) {
                logger.warn("Could not get a result item in recipes.yml ({})", customRecipe.result());
                continue;
            }

            List<String> ingredientsNames = customRecipe.ingredients();

            if (ingredientsNames.isEmpty()) {
                logger.warn("No ingredients specified for {}", customRecipe.result());
                continue;
            }

            if (9 < ingredientsNames.size()) {
                logger.warn("Too many ingredients for {}", customRecipe.result());
                continue;
            }

            BoxItem[] ingredients = new BoxItem[ingredientsNames.size()];
            boolean process = false; // Indicates whether there is at least one or more valid ingredient and there is no unknown ingredient.

            for (int i = 0, s = ingredientsNames.size(); i < s; i++) {
                String ingredientName = ingredientsNames.get(i);

                if (ingredientName.equalsIgnoreCase("air")) {
                    continue;
                }

                Optional<BoxItem> ingredient = itemManager.getBoxItem(ingredientName);

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

        List<IngredientHolder> ingredients = new ArrayList<>();

        Map<Character, RecipeChoice> choices = recipe.getChoiceMap();
        String[] shape = recipe.getShape();
        for (int row = 0; row < shape.length; row++) {
            for (int column = 0; column < shape[row].length(); column++) {
                char symbol = shape[row].charAt(column);
                if (symbol == ' ') {
                    continue;
                }

                IngredientHolder ingredient = this.convertChoice(row * 3 + column, choices.get(symbol));
                if (ingredient == null || ingredient.patterns().isEmpty()) {
                    return;
                }
                ingredients.add(ingredient);
            }
        }

        this.addRecipe(ingredients, result, recipe.getResult().getAmount());
    }

    private void processShapelessRecipe(@NotNull ShapelessRecipe recipe, @NotNull BoxItem result) {
        if (this.recipeConfig.disabledRecipes().contains(recipe.getKey().toString())) {
            return;
        }

        List<IngredientHolder> ingredients = new ArrayList<>();

        int slot = 0;
        for (RecipeChoice choice : recipe.getChoiceList()) {
            IngredientHolder ingredient = this.convertChoice(slot++, choice);
            if (ingredient == null || ingredient.patterns().isEmpty()) {
                return;
            }
            ingredients.add(ingredient);
        }

        this.addRecipe(ingredients, result, recipe.getResult().getAmount());
    }

    private void processCustomRecipe(@Nullable BoxItem @NotNull [] ingredients, @NotNull BoxItem result, int amount) {
        List<IngredientHolder> ingredientHolders = new ArrayList<>();

        for (int i = 0; i < ingredients.length; i++) {
            BoxItem ingredient = ingredients[i];
            if (ingredient != null) {
                ingredientHolders.add(IngredientHolder.fromSingleItem(i, ingredient.getOriginal()));
            }
        }

        this.addRecipe(ingredientHolders, result, amount);
    }

    private @Nullable IngredientHolder convertChoice(int slot, @Nullable RecipeChoice choice) {
        if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
            return IngredientHolder.fromMaterialChoice(slot, materialChoice);
        }
        if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
            return IngredientHolder.fromExactChoice(slot, exactChoice);
        }
        return null;
    }

    void addRecipe(@NotNull List<IngredientHolder> ingredients, @NotNull BoxItem result, int amount) {
        BoxItemRecipe recipe = new BoxItemRecipe(ingredients, result, amount);
        this.recipeMap.computeIfAbsent(result, i -> new RecipeHolder()).addRecipe(recipe);
        BoxAPI.api().getEventCallers().sync().call(new RecipeImportEvent(recipe));
    }
}
