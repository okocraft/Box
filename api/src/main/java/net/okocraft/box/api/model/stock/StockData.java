package net.okocraft.box.api.model.stock;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

/**
 * A record of the stock.
 */
public record StockData(@NotNull BoxItem item, int amount) {
}
