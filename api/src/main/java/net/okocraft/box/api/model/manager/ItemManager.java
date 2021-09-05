package net.okocraft.box.api.model.manager;

import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * An interface to manage {@link BoxItem}s.
 */
public interface ItemManager {

    /**
     * Gets the {@link BoxItem} of the specified item if it exists.
     *
     * @param itemStack the item to get
     * @return the optional {@link BoxItem}
     */
    @NotNull Optional<BoxItem> getBoxItem(@NotNull ItemStack itemStack);

    /**
     * Gets the {@link BoxItem} of the specified name if it exists.
     *
     * @param name the item name to get
     * @return the optional {@link BoxItem}
     */
    @NotNull Optional<BoxItem> getBoxItem(@NotNull String name);

    /**
     * Gets the {@link BoxItem} of the specified id if it exists.
     *
     * @param id the id to get
     * @return the optional {@link BoxItem}
     */
    @NotNull Optional<BoxItem> getBoxItem(int id);

    /**
     * Checks if the specified item has already been registered.
     *
     * @param itemStack the item to check
     * @return {@code true} if registered, {@code false} otherwise
     */
    boolean isRegistered(@NotNull ItemStack itemStack);

    /**
     * Checks if the specified name has already been used.
     *
     * @param name the name to check
     * @return {@code true} if used, {@code false} otherwise
     */
    boolean isUsed(@NotNull String name);

    /**
     * Checks if the specified item is a custom item created by box itself.
     *
     * @param item  the item to check
     * @return {@code true} if the item is a custom item, {@code false} otherwise
     */
    boolean isCustomItem(@NotNull BoxItem item);

    /**
     * Registers item.
     * <p>
     * It will throw an exception in {@link CompletableFuture}
     * if the item is already registered or fails to save to a file or database.
     *
     * @param original the item to register
     * @return the {@link CompletableFuture} to register item
     */
    @NotNull CompletableFuture<@NotNull BoxCustomItem> registerCustomItem(@NotNull ItemStack original);

    /**
     * Rename the custom item.
     *
     * @param item    the custom item to rename
     * @param newName the new name
     * @return the {@link CompletableFuture} to rename an item
     */
    @NotNull CompletableFuture<@NotNull BoxCustomItem> renameCustomItem(@NotNull BoxCustomItem item,
                                                                        @NotNull String newName);

    /**
     * Gets the item name set.
     * <p>
     * Returns set is a collection of {@link BoxItem#getPlainName()}.
     *
     * @return the set of @link BoxItem#getPlainName()}
     */
    @NotNull @Unmodifiable Set<String> getItemNameSet();

    /**
     * Gets all {@link BoxItem}s.
     *
     * @return the set of all {@link BoxItem}s
     */
    @NotNull @Unmodifiable Collection<BoxItem> getBoxItemSet();
}
