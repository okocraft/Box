package net.okocraft.box.core.model.manager.user;

import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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
            user = this.userStorage.loadBoxUser(uuid);
        } catch (Exception e) {
            BoxLogger.logger().error("Could not load the user ({})", uuid, e);
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
            BoxLogger.logger().error("Could not search for the user by name ({})", name, e);
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
            BoxLogger.logger().error("Could not save the user (uuid: {} name: {})", user.getUUID(), name, e);
        }
    }
}
