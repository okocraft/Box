package net.okocraft.box.api.model.user;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * An interface of a box user.
 */
public interface BoxUser {

    /**
     * Gets the {@link UUID} of this user.
     *
     * @return the {@link UUID} of this user.
     */
    @NotNull UUID getUUID();

    /**
     * Gets the name of this user.
     *
     * @return the name of this user
     */
    @NotNull Optional<String> getName();
}

