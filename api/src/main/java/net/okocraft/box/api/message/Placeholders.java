package net.okocraft.box.api.message;

import com.github.siroshun09.messages.minimessage.base.Placeholder;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

/**
 * A collection of common {@link Placeholder}s.
 */
public final class Placeholders {

    /**
     * A {@link Placeholder} of {@code <permission>}.
     */
    public static final Placeholder<String> PERMISSION = Placeholder.component("permission", Component::text);

    /**
     * A {@link Placeholder} of {@code <player_name>}.
     */
    public static final Placeholder<String> PLAYER_NAME = Placeholder.component("player_name", Component::text);

    /**
     * A {@link Placeholder} of {@code <item>}.
     */
    public static final Placeholder<BoxItem> ITEM = Placeholder.component("item", Placeholders::render);

    /**
     * A {@link Placeholder} of {@code <item_name>}.
     */
    public static final Placeholder<String> ITEM_NAME = Placeholder.component("item_name", Component::text);

    /**
     * A {@link Placeholder} of {@code <amount>}.
     */
    public static final Placeholder<Integer> AMOUNT = Placeholder.component("amount", Component::text);

    /**
     * A {@link Placeholder} of {@code <current>}.
     */
    public static final Placeholder<Integer> CURRENT = Placeholder.component("current", Component::text);

    /**
     * A {@link Placeholder} of {@code <error>}.
     */
    public static final Placeholder<Throwable> ERROR = Placeholder.component("error", e -> Component.text(e.getMessage()));

    /**
     * A {@link Placeholder} of {@code <arg>}.
     */
    public static final Placeholder<String> ARG = Placeholder.component("arg", Component::text);

    private static @NotNull Component render(@NotNull BoxItem item) {
        return item.getDisplayName().color(null).hoverEvent(item.getOriginal());
    }

    private Placeholders() {
        throw new UnsupportedOperationException();
    }
}
