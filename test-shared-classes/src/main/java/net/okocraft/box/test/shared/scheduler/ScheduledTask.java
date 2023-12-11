package net.okocraft.box.test.shared.scheduler;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public record ScheduledTask(@NotNull Duration delay, @NotNull Duration interval, @NotNull Type type) {

    public enum Type {
        GLOBAL,
        ENTITY,
        ASYNC
    }
}
