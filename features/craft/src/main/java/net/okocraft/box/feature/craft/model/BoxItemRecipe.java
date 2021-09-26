package net.okocraft.box.feature.craft.model;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BoxItemRecipe(@NotNull List<IngredientHolder> ingredients, @NotNull BoxItem result, int amount) {
}
