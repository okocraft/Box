package net.okocraft.box.feature.craft.model2;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SelectedRecipe(@NotNull List<BoxIngredientItem> ingredients, @NotNull BoxItem result, int amount) {
}
