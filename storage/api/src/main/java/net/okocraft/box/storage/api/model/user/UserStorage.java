package net.okocraft.box.storage.api.model.user;

import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface UserStorage {

    void init() throws Exception;

    @NotNull BoxUser getUser(@NotNull UUID uuid) throws Exception;

    void saveBoxUser(@NotNull UUID uuid, @Nullable String name) throws Exception;

    @Nullable BoxUser searchByName(@NotNull String name) throws Exception;

    @NotNull Collection<BoxUser> getAllUsers() throws Exception;
}
