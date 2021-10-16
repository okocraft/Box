package net.okocraft.box.api.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

/**
 * An interface for providing {@link Executor}s.
 */
@Deprecated(forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "4.2.0")
public interface ExecutorProvider {

    /**
     * Gets an executor, not server's main-thread.
     *
     * @return an {@link Executor}
     */
    @NotNull Executor getExecutor();

    /**
     * Gets a server's main-thread.
     *
     * @return a server's main-thread
     */
    @NotNull Executor getMainThread();

}
