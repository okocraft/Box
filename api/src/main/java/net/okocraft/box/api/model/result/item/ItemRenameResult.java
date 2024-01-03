package net.okocraft.box.api.model.result.item;

import net.okocraft.box.api.model.item.BoxCustomItem;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that represents a result of renaming a custom item.
 */
public sealed interface ItemRenameResult permits ItemRenameResult.DuplicateName, ItemRenameResult.ExceptionOccurred, ItemRenameResult.Success {

    /**
     * A record of the {@link ItemRenameResult} that indicates success.
     *
     * @param customItem   a renamed custom item
     * @param previousName a previous name of item
     */
    record Success(@NotNull BoxCustomItem customItem, @NotNull String previousName) implements ItemRenameResult {
    }

    /**
     * A record of the {@link ItemRenameResult} that indicates failure due to a duplicate name.
     *
     * @param name a duplicate name
     */
    record DuplicateName(@NotNull String name) implements ItemRenameResult {
    }

    /**
     * A record of the {@link ItemRenameResult} that indicates failure due to exception occurred.
     *
     * @param exception an {@link Exception}
     */
    record ExceptionOccurred(@NotNull Exception exception) implements ItemRenameResult {
    }

}
