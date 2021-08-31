package net.okocraft.box.api.model.manager;

import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An interface to load a {@link BoxUser}.
 */
public interface UserManager {

    /**
     * Loads the {@link BoxUser} of specified {@link UUID}.
     *
     * @param uuid the {@link UUID} to load
     * @return the {@link CompletableFuture} to load the {@link BoxUser}
     */
    @NotNull CompletableFuture<@NotNull BoxUser> loadUser(@NotNull UUID uuid);

    /**
     * Saves the {@link BoxUser}.
     *
     * @param boxUser the user to save
     * @return the {@link CompletableFuture} to save the {@link BoxUser}
     */
    @NotNull CompletableFuture<Void> saveUser(@NotNull BoxUser boxUser);

    /**
     * Searches for {@link BoxUser} with the specified name.
     *
     * @param name the name to search
     * @return the {@link CompletableFuture} to search for {@link BoxUser}
     */
    @NotNull CompletableFuture<Optional<BoxUser>> search(@NotNull String name);
}
