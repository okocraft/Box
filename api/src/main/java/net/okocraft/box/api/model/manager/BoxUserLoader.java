package net.okocraft.box.api.model.manager;

import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An interface to load a {@link BoxUser}.
 */
public interface BoxUserLoader {

    /**
     * Loads the {@link BoxUser} of specified {@link UUID}.
     *
     * @param uuid the {@link UUID} to load
     * @return the {@link CompletableFuture} to load the {@link BoxUser}
     */
    @NotNull CompletableFuture<@NotNull BoxUser> loadUser(@NotNull UUID uuid);
}
