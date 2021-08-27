package net.okocraft.box.api.model.data;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

/**
 * A record of the stock.
 */
public record BoxStockData(@NotNull BoxItem item, int amount) {
}
