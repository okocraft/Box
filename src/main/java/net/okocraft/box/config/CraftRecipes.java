package net.okocraft.box.config;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.okocraft.box.Box;
import net.okocraft.box.database.ItemData;

public class CraftRecipes extends CustomConfig {

    private final Box plugin = Box.getInstance();

    public CraftRecipes() {
        super("craftrecipes.yml");
    }

    public Map<String, Integer> getIngredients(ItemStack item) {
        String itemName = getItemName(item);
        if (itemName == null) {
            return Map.of();
        }
        
        ConfigurationSection section = get().getConfigurationSection(itemName + ".ingredients");
        if (section == null) {
            return Map.of();
        }

        Map<String, Integer> ingredients = new HashMap<>();
        for (String ingredient : section.getKeys(false)) {
            int ingredientAmount = section.getInt(ingredient);
            if (ingredientAmount == 0) {
                return Map.of();
            }

            if (getItemStack(ingredient) == null) {
                return Map.of();
            }

            ingredients.put(ingredient, ingredientAmount);            
        }

        return ingredients;
    }

    public int getResultAmount(ItemStack item) {
        String itemName = getItemName(item);
        if (itemName == null) {
            return 0;
        }
        return get().getInt(itemName + ".result-amount");
    }

    private String getItemName(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemData itemData = plugin.getAPI().getItemData();
        return itemData.getName(item);
    }

    private ItemStack getItemStack(String name) {
        if (name == null) {
            return null;
        }

        return plugin.getAPI().getItemData().getItemStack(name);
    }
}
