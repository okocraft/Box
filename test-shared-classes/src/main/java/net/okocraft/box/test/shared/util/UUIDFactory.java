package net.okocraft.box.test.shared.util;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class UUIDFactory {

    public static @NotNull UUID byName(@NotNull String name) {
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }

    private UUIDFactory() {
        throw new UnsupportedOperationException();
    }
}
