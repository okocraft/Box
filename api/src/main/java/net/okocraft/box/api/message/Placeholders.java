package net.okocraft.box.api.message;

import dev.siroshun.mcmsgdef.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.okocraft.box.api.model.item.BoxItem;

/**
 * A collection of common {@link Placeholder}s.
 */
public final class Placeholders {

    /**
     * A {@link Placeholder} of {@code <permission>}.
     */
    public static final Placeholder<String> PERMISSION = node -> Argument.string("permission", node);

    /**
     * A {@link Placeholder} of {@code <player_name>}.
     */
    public static final Placeholder<String> PLAYER_NAME = name -> Argument.string("player_name", name);

    /**
     * A {@link Placeholder} of {@code <item>}.
     */
    public static final Placeholder<BoxItem> ITEM = item -> Argument.component("item", item.getDisplayName().color(null).hoverEvent(item.getOriginal()));

    /**
     * A {@link Placeholder} of {@code <item_name>}.
     */
    public static final Placeholder<String> ITEM_NAME = itemName -> Argument.string("item_name", itemName);

    /**
     * A {@link Placeholder} of {@code <amount>}.
     */
    public static final Placeholder<Integer> AMOUNT = amount -> Argument.numeric("amount", amount);

    /**
     * A {@link Placeholder} of {@code <current>}.
     */
    public static final Placeholder<Integer> CURRENT = current -> Argument.numeric("current", current);

    /**
     * A {@link Placeholder} of {@code <error>}.
     */
    public static final Placeholder<Throwable> ERROR = e -> Argument.string("error", e.getMessage());

    /**
     * A {@link Placeholder} of {@code <arg>}.
     */
    public static final Placeholder<String> ARG = arg -> Argument.string("arg", arg);

    private Placeholders() {
        throw new UnsupportedOperationException();
    }
}
