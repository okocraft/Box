package net.okocraft.box.storage.migrator.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerWrapper {

    private final Logger logger;

    public LoggerWrapper(@Nullable Logger logger) {
        this.logger = logger;
    }

    public void info(@NotNull String log) {
        if (logger != null) {
            logger.info(log);
        }
    }

    public void warning(@NotNull String log) {
        if (logger != null) {
            logger.warning(log);
        }
    }

    public void severe(@NotNull String log) {
        if (logger != null) {
            logger.severe(log);
        }
    }

    public void severe(@NotNull String log, @NotNull Throwable throwable) {
        if (logger != null) {
            logger.log(Level.SEVERE, log, throwable);
        }
    }

    public void severe(@NotNull Throwable throwable) {
        if (logger != null) {
            logger.log(Level.SEVERE, "", throwable);
        }
    }
}
