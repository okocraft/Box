package net.okocraft.box.api.model.user;

import org.jetbrains.annotations.NotNull;

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
}

