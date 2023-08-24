package net.okocraft.box.api.model.result.item;

import net.okocraft.box.api.model.item.BoxCustomItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public sealed interface ItemRegistrationResult permits ItemRegistrationResult.DuplicateItem, ItemRegistrationResult.DuplicateName, ItemRegistrationResult.ExceptionOccurred, ItemRegistrationResult.Success {

    record Success(@NotNull BoxCustomItem customItem) implements ItemRegistrationResult {
    }

    record DuplicateName(@NotNull String name) implements ItemRegistrationResult {
    }

    record DuplicateItem(@NotNull ItemStack item) implements ItemRegistrationResult {
    }

    record ExceptionOccurred(@NotNull Exception exception) implements ItemRegistrationResult {
    }

}
