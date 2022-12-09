package net.okocraft.box.feature.category.api.category;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.Material;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * An interface that represents the category into which items are grouped.
 */
public interface Category {

    /**
     * Creates a new {@link Category}.
     *
     * @param displayName  the {@link Component} to display the name
     * @param iconMaterial the {@link Material} to display the icon
     * @return a new {@link Category}
     */
    @Contract("_, _ -> new")
    static @NotNull Category create(@NotNull Component displayName, @NotNull Material iconMaterial) {
        return create(displayName, iconMaterial, true);
    }

    /**
     * Creates a new {@link Category}.
     *
     * @param displayName  the {@link Component} to display the name
     * @param iconMaterial the {@link Material} to display the icon
     * @param shouldSave   whether the category should be saved to {@code categories.yml}.
     * @return a new {@link Category}
     */
    @Contract("_, _, _ -> new")
    static @NotNull Category create(@NotNull Component displayName, @NotNull Material iconMaterial, boolean shouldSave) {
        return new CategoryImpl(displayName, iconMaterial, shouldSave);
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    @NotNull Component getDisplayName();

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
    @NotNull @Unmodifiable List<BoxItem> getItems();

    /**
     * Adds a {@link BoxItem} to this {@link Category}.
     *
     * @param item {@link BoxItem} to add to this {@link Category}
     */
    void addItem(@NotNull BoxItem item);

    /**
     * Removes a {@link BoxItem} from this {@link Category}.
     *
     * @param item {@link BoxItem} to remove from this {@link Category}
     */
    void removeItem(@NotNull BoxItem item);

    /**
     * Returns whether the category should be saved to {@code categories.yml}.
     *
     * @return whether the category should be saved to {@code categories.yml}.
     */
    boolean shouldSave();
}
