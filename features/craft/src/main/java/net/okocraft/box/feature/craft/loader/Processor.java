package net.okocraft.box.feature.craft.loader;

import com.github.siroshun09.configapi.api.Configuration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class Processor {

    private static final Set<ItemStack> DISABLED_ITEM = Set.of(new ItemStack(Material.FIREWORK_ROCKET));

    private final Configuration recipeConfig;
    private final ItemManager itemManager = BoxProvider.get().getItemManager();
    private final Map<BoxItem, RecipeHolder> recipeMap = new HashMap<>(100, 0.8f);

    Processor(@NotNull Configuration recipeConfig) {
        this.recipeConfig = recipeConfig;
    }

    void processRecipe(@NotNull Recipe recipe) {
        var item = recipe.getResult().clone();

        item.setAmount(1);

        if (DISABLED_ITEM.contains(item)) {
            return;
        }

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

    void processCustomRecipes() {
        var section = recipeConfig.getSection("custom-recipes");

        if (section == null) {
            return;
        }

        var logger = BoxProvider.get().getLogger();
        var itemManager = BoxProvider.get().getItemManager();

        for (var key : section.getKeyList()) {
            var ingredients = new ArrayList<BoxItem>();

            for (var ingredientItemName : section.getStringList(key + ".ingredients")) {
                var item = itemManager.getBoxItem(ingredientItemName);

                if (item.isEmpty()) {
                    logger.warning("Could not get an ingredient item in recipes.yml (" + ingredientItemName + ")");
                    ingredients.clear();
                    break;
                } else {
                    ingredients.add(item.get());
                }
            }

            if (ingredients.isEmpty()) {
                continue;
            }

            var resultItemName = section.getString(key + ".result-item");
            var resultItem = itemManager.getBoxItem(resultItemName);

            if (resultItem.isEmpty()) {
                logger.warning("Could not get a result item in recipes.yml (" + resultItemName + ")");
                continue;
            }

            var amount = Math.max(1, section.getInteger(key + ".amount"));
            processCustomRecipe(ingredients, resultItem.get(), amount);
        }
    }

    @NotNull Map<BoxItem, RecipeHolder> result() {
        return recipeMap;
    }

    private void processShapedRecipe(@NotNull ShapedRecipe recipe, @NotNull BoxItem result) {
        if (recipeConfig.getString("disabled-recipes").contains(recipe.getKey().toString())) {
            return;
        }

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
        if (recipeConfig.getString("disabled-recipes").contains(recipe.getKey().toString())) {
            return;
        }

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

    private void processCustomRecipe(@NotNull List<BoxItem> ingredients, @NotNull BoxItem result, int amount) {
        var ingredientHolders = new ArrayList<IngredientHolder>();

        int slot = 0;
        for (var ingredient : ingredients) {
            ingredientHolders.add(IngredientHolder.fromSingleItem(slot, ingredient.getOriginal()));
            slot++;
        }

        getRecipeHolder(result).addRecipe(new BoxItemRecipe(ingredientHolders, result, amount));
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
