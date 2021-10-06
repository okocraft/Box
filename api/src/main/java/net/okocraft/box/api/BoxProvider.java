package net.okocraft.box.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class to provide {@link BoxAPI}.
 */
public final class BoxProvider {

    private static BoxAPI API;

    /**
     * Gets a {@link BoxAPI}.
     *
     * @return a {@link BoxAPI}
     * @throws IllegalStateException if {@link BoxAPI} is not loaded
     */
    public static @NotNull BoxAPI get() throws IllegalStateException {
        if (API != null) {
            return API;
        } else {
            throw new IllegalStateException("BoxAPI is not loaded.");
        }
    }

    /**
     * Sets a {@link BoxAPI}.
     *
     * @param api a {@link BoxAPI}
     * @throws IllegalStateException if {@link BoxAPI} is already set
     */
    public static void set(@NotNull BoxAPI api) {
        if (API == null) {
            API = Objects.requireNonNull(api);
        } else {
            throw new IllegalStateException("BoxAPI is already set.");
        }
    }
}
