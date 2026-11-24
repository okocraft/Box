package net.okocraft.box.feature.autostore.event;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.stockholder.stock.StockActor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A notification fired when AutoStore takes an item from a pickup or a drop.
 * It is delivered on the thread handling the operation, after the item is removed from its source.
 */
@NotNullByDefault
public final class AutoStoreItemEvent extends BoxEvent {

    private final StockActor.Player actor;
    private final Location location;
    private final ItemStack item;
    private final @Nullable UUID sourceEntityId;

    /**
     * @param actor the player associated with the item
     * @param location the original pickup or drop location
     * @param item the item AutoStore took
     * @param sourceEntityId the UUID of the item or arrow entity that was collected,
     *                       or {@code null} if the item was taken from a drop list without an entity
     */
    public AutoStoreItemEvent(StockActor.Player actor, Location location, ItemStack item, @Nullable UUID sourceEntityId) {
        this.actor = actor;
        this.location = location.clone();
        this.item = item.clone();
        this.sourceEntityId = sourceEntityId;
    }

    public StockActor.Player getActor() {
        return this.actor;
    }

    /**
     * Returns a copy of the original pickup or drop location.
     */
    public Location getLocation() {
        return this.location.clone();
    }

    /**
     * Returns a copy of the item AutoStore took.
     */
    public ItemStack getItem() {
        return this.item.clone();
    }

    /**
     * Returns the UUID of the collected item or arrow entity, or {@code null} if there was no such entity.
     */
    public @Nullable UUID getSourceEntityId() {
        return this.sourceEntityId;
    }
}
