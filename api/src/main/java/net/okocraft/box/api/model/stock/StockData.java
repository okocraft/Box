package net.okocraft.box.api.model.stock;

/**
 * A record of the stock.
 *
 * @param itemId {@link net.okocraft.box.api.model.item.BoxItem#getInternalId()}
 * @param amount the amount of the stock
 */
public record StockData(int itemId, int amount) {
}
