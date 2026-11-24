package net.okocraft.box.feature.stick.event;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.stockholder.stock.StockActor;
import net.okocraft.box.feature.stick.function.container.ContainerContents;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/**
 * A notification fired on the thread handling a Box Stick operation after a physical container's
 * contents have changed. Pure slot moves and changes in how stacks are split do not fire this event.
 */
@NotNullByDefault
public final class StickContainerChangeEvent extends BoxEvent {

    private final StockActor.Player actor;
    private final Location location;
    private final @Nullable Material containerType;
    private final @Nullable ItemStack[] before;
    private final @Nullable ItemStack[] after;

    /**
     * Creates an event without a container type snapshot.
     *
     * @see #StickContainerChangeEvent(StockActor.Player, Location, Material, ItemStack[], ItemStack[])
     */
    public StickContainerChangeEvent(StockActor.Player actor, Location location,
                                     @Nullable ItemStack[] before, @Nullable ItemStack[] after) {
        this(actor, location, null, before, after);
    }

    /**
     * @param actor the player who used the Box Stick
     * @param location the clicked block of the container
     * @param containerType the clicked block type, or null if unavailable
     * @param before the container's slot contents before the operation
     * @param after the container's slot contents after the operation
     */
    public StickContainerChangeEvent(StockActor.Player actor, Location location, @Nullable Material containerType,
                                     @Nullable ItemStack[] before, @Nullable ItemStack[] after) {
        this.actor = actor;
        this.location = location.clone();
        this.containerType = containerType;
        this.before = ContainerContents.copy(before);
        this.after = ContainerContents.copy(after);
    }

    public StockActor.Player getActor() {
        return this.actor;
    }

    /**
     * Returns a copy of the location of the clicked block.
     */
    public Location getLocation() {
        return this.location.clone();
    }

    /**
     * Returns the clicked block type captured before the operation, or null if unavailable.
     */
    public @Nullable Material getContainerType() {
        return this.containerType;
    }

    /**
     * Returns an independent copy of the contents before the operation, in slot order.
     */
    public @Nullable ItemStack[] getBefore() {
        return ContainerContents.copy(this.before);
    }

    /**
     * Returns an independent copy of the contents after the operation, in slot order.
     */
    public @Nullable ItemStack[] getAfter() {
        return ContainerContents.copy(this.after);
    }
}
