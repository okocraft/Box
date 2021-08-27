package net.okocraft.box.api.model.manager;

import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

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
     * Checks if the specified item has already been registered.
     *
     * @param itemStack the item to check
     * @return {@code true} if registered, {@code false} otherwise
     */
    boolean isRegistered(@NotNull ItemStack itemStack);

    /**
     * Registers item.
     * <p>
     * Only uppercase letters and {@code _} are allowed in the plain name.
     * <p>
     * If {@link #isRegistered(ItemStack)} is {@code false}
     * and the plain name also satisfies the requirement, it can be registered successfully.
     * <p>
     * It will throw an exception in {@link CompletableFuture}
     * if the item is already registered, has an invalid plain name,
     * or fails to save to a file or database.
     *
     * @param original  the item to register
     * @param plainName the item name
     * @return the {@link CompletableFuture} to register item
     */
    @NotNull CompletableFuture<@NotNull BoxCustomItem> registerCustomItem(@NotNull ItemStack original,
                                                                          @NotNull String plainName);

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
    @NotNull @Unmodifiable Set<BoxItem> getBoxItemSet();
}
