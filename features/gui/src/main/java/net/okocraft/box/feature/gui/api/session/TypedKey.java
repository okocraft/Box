package net.okocraft.box.feature.gui.api.session;

import org.jetbrains.annotations.NotNull;

public record TypedKey<T>(@NotNull Class<T> clazz, @NotNull String key) {

    public static <T> @NotNull TypedKey<T> of(@NotNull Class<T> clazz, @NotNull String key) {
        return new TypedKey<>(clazz, key);
    }
}
