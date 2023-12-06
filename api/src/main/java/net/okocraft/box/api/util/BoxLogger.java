package net.okocraft.box.api.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLogger;

/**
 * A class to get Box's logger.
 */
@ApiStatus.Internal
public final class BoxLogger {

    private static final SubstituteLogger LOGGER = new SubstituteLogger("Box", null, true);

    /**
     * Gets Box's {@link Logger}.
     *
     * @return Box's {@link Logger}.
     */
    public static @NotNull Logger logger() {
        return LOGGER;
    }

    private BoxLogger() {
        throw new UnsupportedOperationException();
    }
}
