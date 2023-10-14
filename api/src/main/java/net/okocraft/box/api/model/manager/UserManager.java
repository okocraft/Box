package net.okocraft.box.api.model.manager;

import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An interface to load a {@link BoxUser}.
 */
public interface UserManager {

    /**
     * Creates a {@link BoxUser} with a {@link UUID}.
     *
     * @param uuid the user's {@link UUID}
     * @return a new {@link BoxUser}
     */
    @NotNull BoxUser createBoxUser(@NotNull UUID uuid);

    /**
     * Creates a {@link BoxUser} with a {@link UUID} and a name.
     *
     * @param uuid the user's {@link UUID}
     * @param name the user's name
     * @return a new {@link BoxUser}
     */
    @NotNull BoxUser createBoxUser(@NotNull UUID uuid, @NotNull String name);

    /**
     * Loads the {@link BoxUser} of specified {@link UUID}.
     *
     * @param uuid the {@link UUID} to load
     * @return the loaded {@link BoxUser}
     */
    @NotNull BoxUser loadBoxUser(@NotNull UUID uuid);

    /**
     * Searches for {@link BoxUser} with the specified name.
     *
     * @param name the name to search
     * @return the found {@link BoxUser} or {@code null} if not found
     */
    @Nullable BoxUser searchByName(@NotNull String name);

    /**
     * Loads the {@link BoxUser} of specified {@link UUID}.
     *
     * @param uuid the {@link UUID} to load
     * @return the {@link CompletableFuture} to load the {@link BoxUser}
     * @deprecated user {@link #loadBoxUser(UUID)}
     */
    @Deprecated(since = "5.5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default @NotNull CompletableFuture<@NotNull BoxUser> loadUser(@NotNull UUID uuid) {
        return CompletableFuture.completedFuture(loadBoxUser(uuid));
    }

    /**
     * Searches for {@link BoxUser} with the specified name.
     *
     * @param name the name to search
     * @return the {@link CompletableFuture} to search for {@link BoxUser}
     * @deprecated use {@link #searchByName(String)}
     */
    @Deprecated(since = "5.5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default @NotNull CompletableFuture<Optional<BoxUser>> search(@NotNull String name) {
        return CompletableFuture.completedFuture(Optional.ofNullable(searchByName(name)));
    }
}
