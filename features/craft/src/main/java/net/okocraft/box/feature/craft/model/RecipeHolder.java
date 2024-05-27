package net.okocraft.box.feature.craft.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that holds multiple {@link BoxItemRecipe}s.
 */
public class RecipeHolder {

    private final List<BoxItemRecipe> recipeList = new ArrayList<>();

    /**
     * Adds the {@link BoxItemRecipe}.
     *
     * @param recipe the {@link BoxItemRecipe} to add
     */
    public void addRecipe(@NotNull BoxItemRecipe recipe) {
        this.recipeList.add(recipe);
    }

    /**
     * Returns the {@link BoxItemRecipe} list.
     *
     * @return the {@link BoxItemRecipe} list
     */
    public @NotNull List<BoxItemRecipe> getRecipeList() {
        return this.recipeList;
    }
}
