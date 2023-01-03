package net.okocraft.box.storage.api.holder;

import net.okocraft.box.api.BoxAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

/**
 * A class to get plugin's {@link java.util.logging.Logger} without using {@link BoxAPI#getLogger()}.
 */
public final class LoggerHolder {

    private static final Logger LOGGER = JavaPlugin.getProvidingPlugin(LoggerHolder.class).getLogger();

    public static @NotNull Logger get() {
        return LOGGER;
    }

    private LoggerHolder() {
        throw new UnsupportedOperationException();
    }
}
