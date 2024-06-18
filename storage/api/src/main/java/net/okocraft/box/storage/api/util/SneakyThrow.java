package net.okocraft.box.storage.api.util;

import org.jetbrains.annotations.NotNull;

public final class SneakyThrow {

    public static void sneaky(@NotNull Throwable exception) {
        throwSneaky(exception);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwSneaky(@NotNull Throwable exception) throws T {
        throw (T) exception;
    }

    private SneakyThrow() {
        throw new UnsupportedOperationException();
    }
}
