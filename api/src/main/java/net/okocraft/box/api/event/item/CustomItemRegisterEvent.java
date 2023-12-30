package net.okocraft.box.api.event.item;

import net.okocraft.box.api.model.item.BoxCustomItem;
import org.jetbrains.annotations.NotNull;

/**
 * An event that is called when a new {@link BoxCustomItem} has been registered.
 */
public class CustomItemRegisterEvent extends ItemEvent {

    /**
     * The constructor of {@link CustomItemRegisterEvent}.
     *
     * @param newItem a registered {@link BoxCustomItem}
     */
    public CustomItemRegisterEvent(@NotNull BoxCustomItem newItem) {
        super(newItem);
    }

}
