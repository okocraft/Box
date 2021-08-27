package net.okocraft.box.api.util;

import net.okocraft.box.api.BoxProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class Debugger {

    public static boolean ENABLED;

    public static void log(@NotNull Supplier<@NotNull String> log) {
        if (ENABLED) {
            BoxProvider.get().getLogger().info("DEBUG: " + log.get());
        }
    }
}
