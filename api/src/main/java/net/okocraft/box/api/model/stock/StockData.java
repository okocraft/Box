package net.okocraft.box.api.model.stock;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A record of the stock.
 */
public record StockData(@NotNull BoxItem item, int amount) {

    public StockData(@NotNull BoxItem item, int amount) {
        this.item = Objects.requireNonNull(item);
        this.amount = amount;
    }
}
