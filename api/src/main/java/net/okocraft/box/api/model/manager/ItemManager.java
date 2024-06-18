package net.okocraft.box.api.model.manager;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.result.item.ItemRegistrationResult;
import net.okocraft.box.api.model.result.item.ItemRenameResult;
import net.okocraft.box.api.util.MCDataVersion;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

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
     * Gets the {@link BoxItem} of the specified id if it exists.
     *
     * @param id the id to get
     * @return the {@link BoxItem} or null
     */
    @Nullable BoxItem getBoxItemOrNull(int id);

    /**
     * Gets a list of item ids.
     * <p>
     * This method returns a collection of {@link BoxItem#getInternalId()}.
     *
     * @return an immutable list of {@link BoxItem#getInternalId()}
     */
    @NotNull IntImmutableList getItemIdList();

    /**
     * Gets a list of item names
     * <p>
     * This method returns a collection of {@link BoxItem#getPlainName()}.
     *
     * @return an immutable list of {@link BoxItem#getPlainName()}
     */
    @NotNull ObjectImmutableList<String> getItemNameList();

    /**
     * Gets a list of registered {@link BoxItem}s.
     *
     * @return an immutable list of {@link BoxItem}s
     */
    @NotNull ObjectImmutableList<BoxItem> getItemList();

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
    boolean isUsedName(@NotNull String name);

    /**
     * Checks if the specified item is a custom item created by box itself.
     *
     * @param item the item to check
     * @return {@code true} if the item is a custom item, {@code false} otherwise
     */
    boolean isCustomItem(@NotNull BoxItem item);

    /**
     * Registers a new item.
     * <p>
     * The registration process will be executed on different thread, and this method does not wait for that process to complete.
     * The {@link Consumer} of {@link ItemRegistrationResult} will be called on that thread.
     *
     * @param original       the item to register
     * @param plainName      the name of the item
     * @param resultConsumer a {@link Consumer}, which will be called when completed registration process
     */
    void registerCustomItem(@NotNull ItemStack original, @Nullable String plainName, @NotNull Consumer<ItemRegistrationResult> resultConsumer);

    /**
     * Renames a {@link BoxCustomItem}.
     * <p>
     * The rename process will be executed on different thread, and this method does not wait for that process to complete.
     * The {@link Consumer} of {@link ItemRegistrationResult} will be called on that thread.
     *
     * @param item           a {@link BoxCustomItem} to rename
     * @param newName        a new name
     * @param resultConsumer a {@link Consumer}, which will be called when completed rename process
     * @throws IllegalArgumentException {@link BoxCustomItem} is not created by Box ({@link #isCustomItem(BoxItem)} returns {@code false})
     */
    void renameCustomItem(@NotNull BoxCustomItem item, @NotNull String newName, @NotNull Consumer<ItemRenameResult> resultConsumer);

    /**
     * Get the {@link UnaryOperator} that converts the default item name
     * from the specified {@link MCDataVersion} to the current {@link MCDataVersion}.
     *
     * @param sourceVersion the version of item names to be converted
     * @return the {@link UnaryOperator} that converts the default item name
     */
    @NotNull UnaryOperator<String> getItemNameConverter(@NotNull MCDataVersion sourceVersion);

    /**
     * Get the remapped item ids.
     *
     * @return the remapped item ids
     */
    @NotNull
    Int2IntMap getRemappedItemIds();

}
