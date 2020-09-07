package net.okocraft.box.plugin.config;

import com.github.siroshun09.configapi.bukkit.BukkitConfig;
import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.model.item.BoxRecipe;
import net.okocraft.box.plugin.model.item.Item;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RecipeConfig extends BukkitConfig {

    private final Box plugin;

    private final Set<BoxRecipe> recipes;

    public RecipeConfig(@NotNull Box plugin) {
        super(plugin, "config.yml", true);

        this.plugin = plugin;
        this.recipes = new HashSet<>();

        loadRecipes();
    }

    @Override
    public boolean reload() {
        if (super.reload()) {
            recipes.clear();
            loadRecipes();
            return true;
        } else {
            return false;
        }
    }

    @NotNull
    public Optional<BoxRecipe> getCustomRecipe(@NotNull ItemStack item) {
        Optional<Item> boxItem = plugin.getItemManager().getItem(item);
        return boxItem.flatMap(value -> recipes.stream().filter(r -> r.getResultAsItem().equals(value)).findFirst());
    }

    private void loadRecipes() {
        Set<BoxRecipe> recipeSet = new HashSet<>();

        for (String resultItemName : getKeys()) {
            BoxRecipe recipe = loadRecipe(resultItemName);

            if (recipe != null) {
                recipeSet.add(recipe);
            } else {
                plugin.getLogger().warning("Invalid recipe setting: " + resultItemName);
            }
        }

        recipes.addAll(recipeSet);
    }

    @Nullable
    private BoxRecipe loadRecipe(@NotNull String name) {
        Optional<Item> item = plugin.getItemManager().getItemByName(name);

        if (item.isEmpty()) {
            plugin.getLogger().warning("Unknown item name" + name);
            return null;
        }

        ConfigurationSection section = getConfig().getConfigurationSection(item.get().getName() + ".ingredients");

        if (section == null) {
            return null;
        }

        Map<Item, Integer> ingredients = new HashMap<>();

        for (String ingredient : section.getKeys(false)) {
            Optional<Item> ingredientItem = plugin.getItemManager().getItemByName(ingredient);

            if (ingredientItem.isEmpty()) {
                plugin.getLogger().warning("Unknown item name: " + ingredient);
                return null;
            }

            int amount = section.getInt(ingredient + ".amount");

            if (amount < 1) {
                plugin.getLogger().warning("Ignore "+  name + " because the requirement is less than 1");
                return null;
            }

            ingredients.put(ingredientItem.get(), amount);
        }

        return new BoxRecipe(item.get(), getInt(item.get().getName() + ".result-amount", 1) ,ingredients);
    }
}
