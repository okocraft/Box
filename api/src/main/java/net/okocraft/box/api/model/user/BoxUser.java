package net.okocraft.box.api.model.user;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * An interface of box user.
 */
public interface BoxUser {

    /**
     * Gets the {@link UUID} of the user.
     *
     * @return the {@link UUID} of the user.
     */
    @NotNull UUID getUUID();
}

