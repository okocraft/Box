package net.okocraft.box.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    /**
     * Sets a {@link BoxAPI}.
     *
     * @param api a {@link BoxAPI}
     * @throws IllegalStateException if {@link BoxAPI} is already set
     * @deprecated this method is for internal, and will be removed in v6
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @ApiStatus.Internal
    public static void set(@NotNull BoxAPI api) {
        if (API == null) {
            API = Objects.requireNonNull(api);
        } else {
            throw new IllegalStateException("BoxAPI is already set.");
        }
    }
}
