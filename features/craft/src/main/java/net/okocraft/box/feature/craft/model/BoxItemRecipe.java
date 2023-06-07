package net.okocraft.box.feature.craft.model;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A record to define the recipe that can be crafted in Box.
 *
 * @param ingredients the required ingredients of this recipe
 * @param result      the result {@link BoxItem} of this recipe
 * @param amount      the amount of the {@link BoxItem} obtained by this recipe
 */
public record BoxItemRecipe(@NotNull List<IngredientHolder> ingredients, @NotNull BoxItem result, int amount) {
}
