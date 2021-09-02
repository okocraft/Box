package net.okocraft.box.core.storage.model.user;

import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface UserStorage {

    void init() throws Exception;

    void close() throws Exception;

    @NotNull BoxUser getUser(@NotNull UUID uuid) throws Exception;

    void saveBoxUser(@NotNull BoxUser user) throws Exception;

    @NotNull Optional<BoxUser> search(@NotNull String name) throws Exception;
}
