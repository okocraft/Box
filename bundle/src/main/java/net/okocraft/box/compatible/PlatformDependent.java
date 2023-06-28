package net.okocraft.box.compatible;

import net.okocraft.box.api.util.Folia;
import net.okocraft.box.core.util.executor.ExecutorProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class PlatformDependent {

    public static @NotNull ExecutorProvider createExecutorProvider(@NotNull Logger logger) {
        if (Folia.check()) {

        } else {

        }
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private PlatformDependent() {
        throw new UnsupportedOperationException();
    }
}
