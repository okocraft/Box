package net.okocraft.box.feature.craft.model;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

/**
 * A record to define the ingredient that uses to craft the item.
 *
 * @param item   the item to craft
 * @param amount the required amount to craft
 */
public record BoxIngredientItem(@NotNull BoxItem item, int amount) {
}
