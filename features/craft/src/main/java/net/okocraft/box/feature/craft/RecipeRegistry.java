package net.okocraft.box.feature.craft;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public final class RecipeRegistry {

    private static Map<BoxItem, RecipeHolder> RECIPE_MAP = Collections.emptyMap();

    public static @Nullable RecipeHolder getRecipes(@NotNull BoxItem item) {
        return RECIPE_MAP.get(item);
    }

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
