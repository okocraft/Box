package net.okocraft.box.feature.craft.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecipeHolder {

    private final List<BoxItemRecipe> recipeList = new ArrayList<>();

    public void addRecipe(@NotNull BoxItemRecipe recipe) {
        recipeList.add(recipe);
    }

    public @NotNull List<BoxItemRecipe> getRecipeList() {
        return recipeList;
    }
}
