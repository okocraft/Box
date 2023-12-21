package net.okocraft.box.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A class to provide {@link BoxAPI}.
 */
@ApiStatus.Obsolete
public final class BoxProvider {

    static BoxAPI API;

    /**
     * Gets a {@link BoxAPI}.
     *
     * @return a {@link BoxAPI}
     * @throws IllegalStateException if {@link BoxAPI} is not loaded
     * @see BoxAPI#api()
     */
    public static @NotNull BoxAPI get() throws IllegalStateException {
        if (API != null) {
            return API;
        } else {
            throw new IllegalStateException("BoxAPI is not loaded.");
        }
    }

    private BoxProvider() {
        throw new UnsupportedOperationException();
    }
}
