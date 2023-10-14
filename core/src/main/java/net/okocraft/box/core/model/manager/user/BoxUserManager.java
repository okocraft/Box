package net.okocraft.box.core.model.manager.user;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.logging.Level;

public class BoxUserManager implements UserManager {

    private final UserStorage userStorage;

    public BoxUserManager(@NotNull UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public @NotNull BoxUser createBoxUser(@NotNull UUID uuid) {
        return BoxUserFactory.create(uuid);
    }

    @Override
    public @NotNull BoxUser createBoxUser(@NotNull UUID uuid, @NotNull String name) {
        return BoxUserFactory.create(uuid, name);
    }

    @Override
    public @NotNull BoxUser loadBoxUser(@NotNull UUID uuid) {
        BoxUser user;

        try {
            user = this.userStorage.getUser(uuid);
        } catch (Exception e) {
            this.logException("load the user (" + uuid + ")", e);
            return this.createBoxUser(uuid); // create a BoxUser without name
        }

        return user;
    }

    @Override
    public @Nullable BoxUser searchByName(@NotNull String name) {
        BoxUser result;

        try {
            result = this.userStorage.searchByName(name);
        } catch (Exception e) {
            this.logException("search for the user by name (" + name + ")", e);
            return null;
        }

        return result;
    }

    public void saveUsername(@NotNull BoxUser user) {
        if (user.getName().isEmpty()) {
            return;
        }

        var name = user.getName().get();

        try {
            this.userStorage.saveBoxUser(user.getUUID(), name);
        } catch (Exception e) {
            this.logException("save the user (uuid: " + user.getUUID() + " name: " + name, e);
        }
    }

    private void logException(@NotNull String action, @NotNull Exception exception) {
        BoxProvider.get().getLogger().log(Level.SEVERE, "Could not " + action, exception);
    }
}
