package net.okocraft.box.feature.craft.loader;

import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class RecipeLoader {

    public static @NotNull Map<BoxItem, RecipeHolder> load() {
        var processor = new Processor();
        Bukkit.recipeIterator().forEachRemaining(processor::processRecipe);
        AdditionalRecipes.RECIPES.forEach(processor::processRecipe);
        return processor.result();
    }

}
