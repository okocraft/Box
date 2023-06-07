package net.okocraft.box.feature.craft;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * A class for managing {@link RecipeHolder}s.
 */
public final class RecipeRegistry {

    private static Map<BoxItem, RecipeHolder> RECIPE_MAP = Collections.emptyMap();

    /**
     * Returns the {@link RecipeHolder} of the specified {@link BoxItem}.
     *
     * @param item the {@link BoxItem} to get
     * @return the {@link RecipeHolder} of the specified {@link BoxItem}, or {@code null} if there is no recipe of the item
     */
    public static @Nullable RecipeHolder getRecipes(@NotNull BoxItem item) {
        return RECIPE_MAP.get(item);
    }

    /**
     * Checks if the {@link BoxItem} has recipes.
     *
     * @param item the {@link BoxItem} to check
     * @return {@code true} if the {@link BoxItem} has recipes, otherwise {@code false}
     */
    public static boolean hasRecipe(@NotNull BoxItem item) {
        return RECIPE_MAP.containsKey(item);
    }

    static void setRecipeMap(@NotNull Map<BoxItem, RecipeHolder> recipeMap) {
        RECIPE_MAP = recipeMap;
    }

    private RecipeRegistry() {
        throw new UnsupportedOperationException();
    }
}
