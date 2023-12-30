package net.okocraft.box.api.event.item;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

/**
 * An event called when the {@link BoxItem} is imported.
 * <p>
 * This event will only be called when the Box is starting.
 */
public class ItemImportEvent extends ItemEvent {

    /**
     * The constructor of a {@link ItemImportEvent}.
     *
     * @param importedItem an imported item
     */
    public ItemImportEvent(@NotNull BoxItem importedItem) {
        super(importedItem);
    }

}
