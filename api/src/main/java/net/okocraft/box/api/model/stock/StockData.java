package net.okocraft.box.api.model.stock;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A record of the stock.
 */
public record StockData(int itemInternalId, int amount) {

    /**
     * The constructor to check arguments.
     *
     * @param item the {@link BoxItem}
     * @param amount the amount of the item
     * @throws NullPointerException if {@code item} is null
     * @deprecated use {@link StockData#StockData(int, int)}
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    public StockData(@NotNull BoxItem item, int amount) {
        this(item.getInternalId(), amount);
    }

    /**
     * @deprecated use {@link net.okocraft.box.api.model.manager.ItemManager} to get a {@link BoxItem} from an internal id
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    public @NotNull BoxItem item() {
        return BoxProvider.get().getItemManager().getBoxItem(itemInternalId).orElseThrow();
    }
}
