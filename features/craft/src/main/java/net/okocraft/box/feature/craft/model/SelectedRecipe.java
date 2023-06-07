package net.okocraft.box.feature.craft.model;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A record to remain the selected recipe.
 *
 * @param ingredients the required ingredients of the selected recipe
 * @param result      the result {@link BoxItem} of the selected recipe
 * @param amount      the amount of the {@link BoxItem} obtained by the selected recipe
 */
public record SelectedRecipe(@NotNull List<BoxIngredientItem> ingredients, @NotNull BoxItem result, int amount) {
}
