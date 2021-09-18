package net.okocraft.box.feature.craft.model;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BoxItemRecipe(@NotNull List<BoxItemIngredient> ingredients,
                            @NotNull BoxItem result, @NotNull RecipePreview preview) {
}
