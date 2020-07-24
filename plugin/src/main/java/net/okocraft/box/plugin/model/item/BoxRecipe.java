package net.okocraft.box.plugin.model.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BoxRecipe implements Recipe {

    private final Item result;
    private final Map<Item, Integer> ingredients;

    public BoxRecipe(@NotNull Item result, @NotNull Map<Item, Integer> ingredients) {
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    @NotNull
    public ItemStack getResult() {
        return result.getOriginalCopy();
    }

    @NotNull
    public Item getResultAsItem() {
        return result;
    }

    @NotNull
    public Map<Item, Integer> getIngredients() {
        return ingredients;
    }
}
