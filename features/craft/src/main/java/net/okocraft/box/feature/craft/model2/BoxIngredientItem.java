package net.okocraft.box.feature.craft.model2;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

public record BoxIngredientItem(@NotNull BoxItem item, int amount) {
}
