package net.okocraft.box.feature.craft.model;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

public record BoxItemIngredient(@NotNull BoxItem item, int amount) {
}
