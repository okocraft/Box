package net.okocraft.box.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class APISetter {

    /**
     * Sets a {@link BoxAPI}.
     *
     * @param api a {@link BoxAPI}
     * @throws IllegalStateException if {@link BoxAPI} is already set
     */
    public static void set(@NotNull BoxAPI api) {
        if (BoxProvider.API == null) {
            BoxProvider.API = Objects.requireNonNull(api);
        } else {
            throw new IllegalStateException("BoxAPI is already set.");
        }
    }

    /**
     * Unsets a {@link BoxAPI}.
     *
     * @throws IllegalStateException if {@link BoxAPI} is not set
     */
    public static void unset() {
        if (BoxProvider.API != null) {
            BoxProvider.API = null;
        } else {
            throw new IllegalStateException("BoxAPI is not set.");
        }
    }

    private APISetter() {
        throw new UnsupportedOperationException();
    }
}
