package net.okocraft.box.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;

public class CraftRecipes {

    private static final Map<String, Map<String, Integer>> recipeMap = new HashMap<>();
    private static final Map<String, Integer> resultAmountMap = new HashMap<>();
    static {
        init();
    }

    public static void load() {}

    public static void init() {
        Items.getItems().stream().map(Items::getItemStack).forEach(item -> {
            String itemName = Items.getName(item, true);

            List<Recipe> recipes = Bukkit.getRecipesFor(Items.getItemStack(itemName));
            recipes.removeIf(recipe -> !(recipe instanceof ShapelessRecipe || recipe instanceof ShapedRecipe));
            if (recipes.size() == 0) {
                return;
            }
            Recipe recipe = recipes.get(0);

            resultAmountMap.put(itemName, recipe.getResult().getAmount());
            
            Collection<ItemStack> ingredients;
            if (recipe instanceof ShapelessRecipe) {
                ingredients = ((ShapelessRecipe) recipe).getIngredientList();
            } else {
                ingredients = ((ShapedRecipe) recipe).getIngredientMap().values();
            }
            ingredients.removeIf(Objects::isNull);

            Map<String, Integer> stacked = new HashMap<>();
            for (ItemStack ingredient : ingredients) {
                String ingredientDBName = Items.getName(ingredient, true);
                stacked.put(ingredientDBName, stacked.getOrDefault(ingredientDBName, 0) + ingredient.getAmount());
            }
            recipeMap.put(itemName, stacked);
        });
    }

    public static Map<String, Integer> getIngredient(ItemStack item) {
        String itemName = Items.getName(item, true);
        if (recipeMap != null && recipeMap.containsKey(itemName)) {
            return recipeMap.get(itemName);
        }
        return Map.of();
    }

    public static int getResultAmount(ItemStack item) {
        return resultAmountMap.getOrDefault(Items.getName(item, true), 0);
    }

    public static List<ItemStack> filterUnavailable(Player player, List<ItemStack> target, long quantity) {
        Map<String, Long> itemAmountMap = PlayerData.getItemsAmount(player);
        return target.parallelStream().filter(item -> {
            Map<String, Integer> ingredients = getIngredient(item);
            if (ingredients.isEmpty()) {
                return false;
            }
            for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
                if (entry.getValue() * quantity > itemAmountMap.getOrDefault(entry.getKey(), 0L).longValue()) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }
}