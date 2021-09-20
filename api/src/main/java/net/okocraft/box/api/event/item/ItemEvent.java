package net.okocraft.box.api.event.item;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class that represents a {@link BoxItem} related event.
 */
public class ItemEvent extends BoxEvent {

    private final BoxItem item;

    /**
     * The constructor of a {@link ItemEvent}.
     *
     * @param item the item of this event
     */
    public ItemEvent(@NotNull BoxItem item) {
        this.item = Objects.requireNonNull(item);
    }

    public @NotNull BoxItem getItem() {
        return item;
    }
}
