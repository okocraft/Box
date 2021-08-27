package net.okocraft.box.api.model.item;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for the added item.
 */
public interface BoxCustomItem extends BoxItem {

    /**
     * Sets the display name
     *
     * @param component the display name
     */
    void setDisplayName(@NotNull Component component);

}
