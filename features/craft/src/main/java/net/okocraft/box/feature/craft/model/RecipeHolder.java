package net.okocraft.box.feature.craft.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeHolder {

    private static final RecipeHolder EMPTY = new RecipeHolder(Collections.emptyList());

    public static @NotNull RecipeHolder empty() {
        return EMPTY;
    }

    private final List<BoxItemRecipe> recipeList;

    public RecipeHolder() {
        this(new ArrayList<>());
    }

    private RecipeHolder(@NotNull List<BoxItemRecipe> recipeList) {
        this.recipeList = recipeList;
    }

    public void addRecipe(@NotNull BoxItemRecipe recipe) {
        recipeList.add(recipe);
    }

    public @NotNull @Unmodifiable List<BoxItemRecipe> getRecipeList() {
        return recipeList;
    }
}
