package net.okocraft.box.api.event.item;

import com.github.siroshun09.event4j.event.Event;
import net.okocraft.box.api.model.item.BoxCustomItem;
import org.jetbrains.annotations.NotNull;

public class CustomItemRegisterEvent extends Event {

    private final BoxCustomItem newItem;

    public CustomItemRegisterEvent(@NotNull BoxCustomItem newItem) {
        this.newItem = newItem;
    }

    public @NotNull BoxCustomItem getNewItem() {
        return newItem;
    }

    @Override
    public String toString() {
        return "CustomItemRegisterEvent{" +
                "newItem=" + newItem +
                '}';
    }
}
