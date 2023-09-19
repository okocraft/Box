package net.okocraft.box.api.model.result.item;

import net.okocraft.box.api.model.item.BoxCustomItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that represents a result of registering / renaming a custom item.
 */
public sealed interface ItemRegistrationResult permits ItemRegistrationResult.DuplicateItem, ItemRegistrationResult.DuplicateName, ItemRegistrationResult.ExceptionOccurred, ItemRegistrationResult.Success {

    /**
     * A record of the {@link ItemRegistrationResult} that indicates success.
     *
     * @param customItem a registered / renamed custom item
     */
    record Success(@NotNull BoxCustomItem customItem) implements ItemRegistrationResult {
    }

    /**
     * A record of the {@link ItemRegistrationResult} that indicates failure due to a duplicate name.
     *
     * @param name a duplicate name
     */
    record DuplicateName(@NotNull String name) implements ItemRegistrationResult {
    }

    /**
     * A record of the {@link ItemRegistrationResult} that indicates failure due to a duplicate item.
     *
     * @param item a duplicate item
     */
    record DuplicateItem(@NotNull ItemStack item) implements ItemRegistrationResult {
    }

    /**
     * A record of the {@link ItemRegistrationResult} that indicates failure due to exception occurred.
     *
     * @param exception an {@link Exception}
     */
    record ExceptionOccurred(@NotNull Exception exception) implements ItemRegistrationResult {
    }

}
