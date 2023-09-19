package net.okocraft.box.api.model.manager;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.result.item.ItemRegistrationResult;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
    void renameCustomItem(@NotNull BoxCustomItem item, @NotNull String newName, @NotNull Consumer<ItemRegistrationResult> resultConsumer);

    /**
     * Registers item.
     * <p>
     * It will throw an exception in {@link CompletableFuture}
     * if the item is already registered or fails to save to a file or database.
     *
     * @param original the item to register
     * @return the {@link CompletableFuture} to register item
     * @deprecated use {@link #registerCustomItem(ItemStack, String, Consumer)}
     */
    @Deprecated(forRemoval = true, since = "5.5.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default @NotNull CompletableFuture<@NotNull BoxCustomItem> registerCustomItem(@NotNull ItemStack original) {
        var future = new CompletableFuture<BoxCustomItem>();

        registerCustomItem(
                original,
                null,
                result -> {
                    if (result instanceof ItemRegistrationResult.Success success) {
                        future.complete(success.customItem());
                    } else if (result instanceof ItemRegistrationResult.DuplicateItem duplicateItem) {
                        future.completeExceptionally(new IllegalStateException("The item is already registered (item: " + duplicateItem.item() + ")"));
                    } else if (result instanceof ItemRegistrationResult.ExceptionOccurred exceptionOccurred) {
                        future.completeExceptionally(exceptionOccurred.exception());
                    }
                }
        );

        return future;
    }

    /**
     * Rename the custom item.
     *
     * @param item    the custom item to rename
     * @param newName the new name
     * @return the {@link CompletableFuture} to rename an item
     * @deprecated use {@link #renameCustomItem(BoxCustomItem, String, Consumer)}
     */
    @Deprecated(forRemoval = true, since = "5.5.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default @NotNull CompletableFuture<@NotNull BoxCustomItem> renameCustomItem(@NotNull BoxCustomItem item, @NotNull String newName) {
        var future = new CompletableFuture<BoxCustomItem>();

        if (!isCustomItem(item)) {
            future.completeExceptionally(new IllegalStateException("Could not rename item because the item is created by box."));
            return future;
        }

        renameCustomItem(
                item,
                newName,
                result -> {
                    if (result instanceof ItemRegistrationResult.Success success) {
                        future.complete(success.customItem());
                    } else if (result instanceof ItemRegistrationResult.DuplicateName duplicateName) {
                        future.completeExceptionally(new IllegalStateException("The name is already used (name: " + duplicateName.name() + ")"));
                    } else if (result instanceof ItemRegistrationResult.ExceptionOccurred exceptionOccurred) {
                        future.completeExceptionally(exceptionOccurred.exception());
                    }
                }
        );

        return future;
    }

    /**
     * Gets the item name set.
     * <p>
     * Returns set is a collection of {@link BoxItem#getPlainName()}.
     *
     * @return the set of {@link BoxItem#getPlainName()}
     * @deprecated use {@link #getItemNameList()}
     */
    @Deprecated(forRemoval = true, since = "5.5.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @NotNull @Unmodifiable Set<String> getItemNameSet();

    /**
     * Gets all {@link BoxItem}s.
     *
     * @return the set of all {@link BoxItem}s
     * @deprecated use {@link #getItemList()}
     */
    @Deprecated(forRemoval = true, since = "5.5.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @NotNull @Unmodifiable Collection<BoxItem> getBoxItemSet();
}
