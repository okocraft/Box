package net.okocraft.box.api.util;

import net.okocraft.box.api.BoxProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A class to log debug information.
 */
public final class Debugger {

    /**
     * Whether it is debug mode or not.
     */
    public static boolean ENABLED;

    /**
     * Logs a debug if {@link #ENABLED} is true.
     *
     * @param logSupplier the supplier of log
     */
    public static void log(@NotNull Supplier<@NotNull String> logSupplier) {
        if (ENABLED) {
            BoxProvider.get().getLogger().info("DEBUG: " + logSupplier.get());
        }
    }
}
