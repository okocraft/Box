package net.okocraft.box.api.event.item;

import net.okocraft.box.api.model.item.BoxCustomItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An event that is called when a new {@link BoxCustomItem} has been renamed.
 */
public class CustomItemRenameEvent extends ItemEvent {

    private final BoxCustomItem renamedItem;
    private final String previousName;

    /**
     * The constructor of a {@link CustomItemRenameEvent}.
     *
     * @param renamedItem  a renamed item
     * @param previousName a previous name of the item
     */
    public CustomItemRenameEvent(@NotNull BoxCustomItem renamedItem, @NotNull String previousName) {
        super(renamedItem);
        this.renamedItem = renamedItem;
        this.previousName = Objects.requireNonNull(previousName);
    }

    /**
     * Gets the previous name of the item.
     *
     * @return the previous name of the item
     */
    public @NotNull String getPreviousName() {
        return previousName;
    }

    @Override
    public String toString() {
        return "CustomItemRenameEvent{" +
                "renamedItem=" + renamedItem +
                ", previousName='" + previousName + '\'' +
                '}';
    }
}
