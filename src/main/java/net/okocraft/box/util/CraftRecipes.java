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

import net.okocraft.box.Box;
import net.okocraft.box.database.ItemData;
import net.okocraft.box.database.PlayerData;

// TODO: itemData.getName(str, bool) メタを無視するかどうかという今までの仕様との整合を考える。
public class CraftRecipes {

    private static final PlayerData playerData = Box.getInstance().getAPI().getPlayerData();
    private static final ItemData itemData = Box.getInstance().getAPI().getItemData();

    private static final Map<String, Map<String, Integer>> recipeMap = new HashMap<>();
    private static final Map<String, Integer> resultAmountMap = new HashMap<>();
    static {
        init();
    }

    public static void load() {}

    public static void init() {
        
        itemData.getNames().forEach(itemName -> {
            ItemStack item = itemData.getItemStack(itemName);

            List<Recipe> recipes = Bukkit.getRecipesFor(item);
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
                String ingredientDBName = itemData.getName(ingredient);
                stacked.put(ingredientDBName, stacked.getOrDefault(ingredientDBName, 0) + ingredient.getAmount());
            }
            recipeMap.put(itemName, stacked);
        });
    }

    public static Map<String, Integer> getIngredient(ItemStack item) {
        String itemName = itemData.getName(item);
        if (recipeMap != null && recipeMap.containsKey(itemName)) {
            return recipeMap.get(itemName);
        }
        return Map.of();
    }

    public static int getResultAmount(ItemStack item) {
        return resultAmountMap.getOrDefault(itemData.getName(item), 0);
    }

    public static List<ItemStack> filterUnavailable(Player player, List<ItemStack> target, long quantity) {
        Map<ItemStack, Integer> stockMap = playerData.getStockAll(player);
        return target.parallelStream().filter(item -> {
            Map<String, Integer> ingredients = getIngredient(item);
            if (ingredients.isEmpty()) {
                return false;
            }
            for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
                if (entry.getValue() * quantity > stockMap.getOrDefault(itemData.getItemStack(entry.getKey()), 0).intValue()) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }
}