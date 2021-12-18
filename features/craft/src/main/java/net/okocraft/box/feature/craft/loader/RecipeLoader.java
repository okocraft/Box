package net.okocraft.box.feature.craft.loader;

import com.github.siroshun09.configapi.api.Configuration;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class RecipeLoader {

    public static @NotNull Map<BoxItem, RecipeHolder> load(@NotNull Configuration recipeConfig) {
        var processor = new Processor(recipeConfig);

        Bukkit.recipeIterator().forEachRemaining(processor::processRecipe);

        AdditionalRecipes.RECIPES.forEach(processor::processRecipe);

        processor.processCustomRecipes();

        return processor.result();
    }
}
