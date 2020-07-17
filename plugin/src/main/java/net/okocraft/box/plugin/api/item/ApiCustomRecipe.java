package net.okocraft.box.plugin.api.item;

import net.okocraft.box.api.item.CustomRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class ApiCustomRecipe implements CustomRecipe {

    private final List<ItemStack> ingredients;
    private final ItemStack result;

    public ApiCustomRecipe(@NotNull List<ItemStack> ingredients, @NotNull ItemStack result) {
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public @NotNull @Unmodifiable List<ItemStack> getIngredients() {
        return List.copyOf(ingredients);
    }

    @Override
    public @NotNull ItemStack getResult() {
        return result;
    }
}
