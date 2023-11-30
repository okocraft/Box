package net.okocraft.box.storage.migrator.util;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerWrapper {

    private final Logger logger;
    private final boolean debug;

    public LoggerWrapper(@NotNull Logger logger, boolean debug) {
        this.logger = logger;
        this.debug = debug;
    }

    public void info(@NotNull String log) {
        logger.info(log);
    }

    public void warning(@NotNull String log) {
        logger.warning(log);
    }

    public void severe(@NotNull String log) {
        logger.severe(log);
    }

    public void severe(@NotNull String log, @NotNull Throwable throwable) {
        logger.log(Level.SEVERE, log, throwable);
    }

    public boolean debug() {
        return debug;
    }
}
