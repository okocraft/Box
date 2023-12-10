package net.okocraft.box.test.shared.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;

import java.util.LinkedList;

public class LogCollector extends AbstractLogger {

    private final LinkedList<Log> collectedLogs = new LinkedList<>();

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
        this.collectedLogs.offer(new Log(level, marker, messagePattern, arguments, throwable));
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return true;
    }

    public void checkLog(@NotNull Level level, @NotNull String pattern) {
        var log = this.collectedLogs.poll();
        Assertions.assertNotNull(log);
        Assertions.assertEquals(level, log.level);
        Assertions.assertEquals(pattern, log.messagePattern);
    }

    public void checkLog(@NotNull Level level, @NotNull String pattern, @Nullable Object @NotNull ... arguments) {
        var log = this.collectedLogs.poll();
        Assertions.assertNotNull(log);
        Assertions.assertEquals(level, log.level);
        Assertions.assertEquals(pattern, log.messagePattern);
        Assertions.assertArrayEquals(arguments, log.arguments);
    }

    public record Log(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
    }
}
