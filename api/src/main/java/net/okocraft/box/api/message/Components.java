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

public final class Components {

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component grayText(@NotNull String text) {
        return text(text, GRAY);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component aquaText(@NotNull String text) {
        return text(text, AQUA);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component whiteText(@NotNull String text) {
        return text(text, WHITE);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component grayText(int value) {
        return text(value, GRAY);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component aquaText(int value) {
        return text(value, AQUA);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component redText(int value) {
        return text(value, RED);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component whiteText(int value) {
        return text(value, WHITE);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component grayTranslatable(@NotNull String key) {
        return translatable(key, GRAY);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component aquaTranslatable(@NotNull String key) {
        return translatable(key, AQUA);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component redTranslatable(@NotNull String key) {
        return translatable(key, RED);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component greenTranslatable(@NotNull String key) {
        return translatable(key, GREEN);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component blackTranslatable(@NotNull String key) {
        return translatable(key, BLACK);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component grayTranslatable(@NotNull String key, @NotNull Component... args) {
        return translatable().key(key).args(args).color(GRAY).build();
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component aquaTranslatable(@NotNull String key, @NotNull Component... args) {
        return translatable().key(key).args(args).color(AQUA).build();
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component redTranslatable(@NotNull String key, @NotNull Component... args) {
        return translatable().key(key).args(args).color(RED).build();
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component greenTranslatable(@NotNull String key, @NotNull Component... args) {
        return translatable().key(key).args(args).color(GREEN).build();
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Component blackTranslatable(@NotNull String key, @NotNull Component... args) {
        return translatable().key(key).args(args).color(BLACK).build();
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component aquaItemName(@NotNull BoxItem item) {
        return item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component aquaItemName(@NotNull ItemStack item) {
        return item.displayName().color(AQUA).hoverEvent(item);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Component commandHelp(@NotNull String keyPrefix) {
        return commandHelp(keyPrefix, true);
    }

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
