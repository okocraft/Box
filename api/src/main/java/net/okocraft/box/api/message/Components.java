package net.okocraft.box.api.message;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLACK;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

/**
 * A class to help reduce codes that create {@link Component}.
 */
public final class Components {

    /**
     * Creates a gray {@link Component} from a string.
     *
     * @param text the text to create {@link Component}
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component grayText(@NotNull String text) {
        return text(text, GRAY);
    }

    /**
     * Creates an aqua {@link Component} from a string.
     *
     * @param text the text to create {@link Component}
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component aquaText(@NotNull String text) {
        return text(text, AQUA);
    }

    /**
     * Creates a white {@link Component} from a string.
     *
     * @param text the text to create {@link Component}
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component whiteText(@NotNull String text) {
        return text(text, WHITE);
    }

    /**
     * Creates a gray {@link Component} from an integer.
     *
     * @param value the integer to create {@link Component}
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component grayText(int value) {
        return text(value, GRAY);
    }

    /**
     * Creates an aqua {@link Component} from an integer.
     *
     * @param value the integer to create {@link Component}
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component aquaText(int value) {
        return text(value, AQUA);
    }

    /**
     * Creates a red {@link Component} from an integer.
     *
     * @param value the integer to create {@link Component}
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component redText(int value) {
        return text(value, RED);
    }

    /**
     * Creates a white {@link Component} from an integer.
     *
     * @param value the integer to create {@link Component}
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component whiteText(int value) {
        return text(value, WHITE);
    }

    /**
     * Creates a gray {@link Component} from the key of the message.
     *
     * @param key the key of the message
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component grayTranslatable(@NotNull String key) {
        return translatable(key, GRAY);
    }

    /**
     * Creates an aqua {@link Component} from the key of the message.
     *
     * @param key the key of the message
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component aquaTranslatable(@NotNull String key) {
        return translatable(key, AQUA);
    }

    /**
     * Creates a red {@link Component} from the key of the message.
     *
     * @param key the key of the message
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component redTranslatable(@NotNull String key) {
        return translatable(key, RED);
    }

    /**
     * Creates a green {@link Component} from the key of the message.
     *
     * @param key the key of the message
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component greenTranslatable(@NotNull String key) {
        return translatable(key, GREEN);
    }

    /**
     * Creates a black {@link Component} from the key of the message.
     *
     * @param key the key of the message
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component blackTranslatable(@NotNull String key) {
        return translatable(key, BLACK);
    }

    /**
     * Creates a gray {@link Component} from the key of the message and arguments.
     *
     * @param key  the key of the message
     * @param args the arguments
     * @return the created {@link Component}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component grayTranslatable(@NotNull String key, @NotNull Component... args) {
        return translatable().key(key).args(args).color(GRAY).build();
    }

    /**
     * Creates an aqua {@link Component} from the key of the message and arguments.
     *
     * @param key  the key of the message
     * @param args the arguments
     * @return the created {@link Component}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component aquaTranslatable(@NotNull String key, @NotNull Component... args) {
        return translatable().key(key).args(args).color(AQUA).build();
    }

    /**
     * Creates a red {@link Component} from the key of the message and arguments.
     *
     * @param key  the key of the message
     * @param args the arguments
     * @return the created {@link Component}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component redTranslatable(@NotNull String key, @NotNull Component... args) {
        return translatable().key(key).args(args).color(RED).build();
    }

    /**
     * Creates a green {@link Component} from the key of the message and arguments.
     *
     * @param key  the key of the message
     * @param args the arguments
     * @return the created {@link Component}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component greenTranslatable(@NotNull String key, @NotNull Component... args) {
        return translatable().key(key).args(args).color(GREEN).build();
    }

    /**
     * Creates a black {@link Component} from the key of the message and arguments.
     *
     * @param key  the key of the message
     * @param args the arguments
     * @return the created {@link Component}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component blackTranslatable(@NotNull String key, @NotNull Component... args) {
        return translatable().key(key).args(args).color(BLACK).build();
    }

    /**
     * Creates an aqua {@link Component} from the {@link BoxItem}.
     * <p>
     * This method will set the item's display name to aqua color
     * and set the item's hover event.
     *
     * @param item the item to create {@link Component}
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component aquaItemName(@NotNull BoxItem item) {
        return item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal());
    }

    /**
     * Creates an aqua {@link Component} from the {@link ItemStack}.
     * <p>
     * This method displays the item's display name in aqua
     * and sets the item's hover event to the component.
     *
     * @param item the item to create {@link Component}
     * @return the created {@link Component}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component aquaItemName(@NotNull ItemStack item) {
        return item.displayName().color(AQUA).hoverEvent(item);
    }

    /**
     * Creates a command help.
     *
     * @param keyPrefix the key prefix of the messages
     * @return the created {@link Component}
     * @see #commandHelp(String, boolean)
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component commandHelp(@NotNull String keyPrefix) {
        return commandHelp(keyPrefix, true);
    }

    /**
     * Creates a command help.
     * <p>
     * This method creates a help message for the command
     * by adding {@code .command-line} and {@code .description} to the prefix of the given key.
     * <p>
     * The keys of the referenced messages are {@code keyPrefix.command-line}
     * and {@code keyPrefix.description}.
     * <p>
     * If {@code appendHelp} is {@code true}, the keys of the messages are
     * {@code keyPrefix.help.command-line} and {@code keyPrefix.help.description}.
     *
     * @param keyPrefix  the key prefix of the messages
     * @param appendHelp whether to append {@code .help} to the key prefix
     * @return the created {@link Component}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component commandHelp(@NotNull String keyPrefix, boolean appendHelp) {
        keyPrefix = appendHelp ? keyPrefix + ".help" : keyPrefix;
        return text()
                .append(aquaTranslatable(keyPrefix + ".command-line"))
                .append(text(" - ", DARK_GRAY))
                .append(grayTranslatable(keyPrefix + ".description"))
                .build();
    }
}
