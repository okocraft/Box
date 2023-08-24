package net.okocraft.box.api.model.item;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An interface of the item that can deposit or withdraw.
 */
public interface BoxItem {

    /**
     * Gets the internal id of this item.
     *
     * @return the internal id of this id
     */
    int getInternalId();

    /**
     * Gets the plain name of this item.
     *
     * @return the plain name of this item
     */
    @NotNull String getPlainName();

    /**
     * Gets the original {@link ItemStack}.
     * <p>
     * The item returned by this method must not be modified.
     * <p>
     * If you want to change it, you should use {@link #getClonedItem()}.
     *
     * @return the original {@link ItemStack}
     */
    @NotNull ItemStack getOriginal();

    /**
     * Gets the cloned {@link ItemStack}.
     *
     * @return the cloned {@link ItemStack}
     */
    @NotNull ItemStack getClonedItem();

    /**
     * Gets the display name of this item as a {@link Component}.
     *
     * @return the display name of this item as a {@link Component}
     */
    @NotNull Component getDisplayName();
}
