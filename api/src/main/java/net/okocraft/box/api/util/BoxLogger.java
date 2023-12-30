package net.okocraft.box.api.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.SubstituteLogger;

/**
 * A class to get Box's logger.
 */
@ApiStatus.Internal
public final class BoxLogger {

    private static final SubstituteLogger LOGGER = new SubstituteLogger("Box", null, true);

    static {
        try {
            Class.forName("org.junit.jupiter.api.Assertions");
            LOGGER.setDelegate(LoggerFactory.getLogger(BoxLogger.class));
        } catch (ClassNotFoundException ignored) {
        }
    }

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
