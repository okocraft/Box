package net.okocraft.box.feature.category.api.category;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

/**
 * An interface that represents the category into which items are grouped.
 */
public interface Category {

    /**
     * Gets the display name.
     *
     * @param viewer a {@link Player} who see the display name
     * @return the display name
     */
    @NotNull Component getDisplayName(@NotNull Player viewer);

    /**
     * Gets the material to create {@link org.bukkit.inventory.ItemStack} as the icon.
     *
     * @return the material to create {@link org.bukkit.inventory.ItemStack} as the icon
     */
    @NotNull Material getIconMaterial();

    /**
     * Gets the list of items that are categorized to this {@link Category}.
     *
     * @return the list of items that are categorized to this {@link Category}
     */
    @NotNull
    @Unmodifiable
    List<BoxItem> getItems();

    /**
     * Adds a {@link BoxItem} to this {@link Category}.
     *
     * @param item {@link BoxItem} to add to this {@link Category}
     */
    void addItem(@NotNull BoxItem item);

    /**
     * Adds {@link BoxItem}s to this {@link Category}.
     *
     * @param items {@link BoxItem}s to add to this {@link Category}
     */
    void addItems(@NotNull Collection<BoxItem> items);

    /**
     * Removes a {@link BoxItem} from this {@link Category}.
     *
     * @param item {@link BoxItem} to remove from this {@link Category}
     */
    void removeItem(@NotNull BoxItem item);

    /**
     * Checks if the {@link BoxItem} is contained in this {@link Category}.
     *
     * @param item the {@link BoxItem} to check
     * @return whether the {@link BoxItem} is contained in this {@link Category}
     */
    boolean containsItem(@NotNull BoxItem item);
}
