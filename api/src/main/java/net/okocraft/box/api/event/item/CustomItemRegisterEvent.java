package net.okocraft.box.api.event.item;

import net.okocraft.box.api.event.AsyncEvent;
import net.okocraft.box.api.model.item.BoxCustomItem;
import org.jetbrains.annotations.NotNull;

/**
 * An event that is called when a new {@link BoxCustomItem} has been registered.
 */
public class CustomItemRegisterEvent extends ItemEvent implements AsyncEvent {

    private final BoxCustomItem newItem;

    /**
     * The constructor of {@link CustomItemRegisterEvent}.
     *
     * @param newItem a registered {@link BoxCustomItem}
     */
    public CustomItemRegisterEvent(@NotNull BoxCustomItem newItem) {
        super(newItem);
        this.newItem = newItem;
    }

    /**
     * Gets the registered {@link BoxCustomItem}.
     *
     * @return the registered {@link BoxCustomItem}
     */
    @Override
    public @NotNull BoxCustomItem getItem() {
        return newItem;
    }

    @Override
    public String toString() {
        return "CustomItemRegisterEvent{" +
                "newItem=" + newItem +
                '}';
    }
}
