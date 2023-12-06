package net.okocraft.box.api.model.data;

import com.github.siroshun09.configapi.api.Configuration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * An interface of container that holds custom data.
 *
 * @deprecated reworked in Box v6.0.0 (in v5, there is no replacement)
 */
@Deprecated(since = "5.5.2", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
public interface CustomDataContainer {

    /**
     * Gets data from the container.
     *
     * @param namespace the namespace
     * @param key       the data key
     * @return the {@link CompletableFuture} to get {@link Configuration}
     */
    @NotNull CompletableFuture<@NotNull Configuration> get(@NotNull String namespace, @NotNull String key);

    /**
     * Sets data to the container.
     *
     * @param namespace     the namespace
     * @param key           the data key
     * @param configuration the data
     * @return the {@link CompletableFuture} to set {@link Configuration}
     */
    @NotNull CompletableFuture<Void> set(@NotNull String namespace, @NotNull String key,
                                         @NotNull Configuration configuration);

}
