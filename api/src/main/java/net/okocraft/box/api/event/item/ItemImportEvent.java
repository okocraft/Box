package net.okocraft.box.api.event.item;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

/**
 * A event called when the {@link BoxItem} is imported.
 * <p>
 * This event is for debugging only, and will only be called when the Box is starting.
 */
public class ItemImportEvent extends ItemEvent {

    private final ItemType type;

    /**
     * The constructor of a {@link ItemImportEvent}.
     *
     * @param importedItem an imported item
     * @param type         a type of item
     */
    public ItemImportEvent(@NotNull BoxItem importedItem, @NotNull ItemType type) {
        super(importedItem);
        this.type = type;
    }

    /**
     * Gets the type of the item.
     *
     * @return the type of the item
     */
    public @NotNull ItemType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ItemImportEvent{" +
                "item=" + getItem() +
                ", type=" + type +
                '}';
    }

    public enum ItemType {
        DEFAULT_ITEM,
        CUSTOM_ITEM
    }
}
