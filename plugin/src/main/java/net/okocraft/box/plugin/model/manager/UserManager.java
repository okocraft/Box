package net.okocraft.box.plugin.model.manager;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.result.UserCheckResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class UserManager {

    private final Box plugin;
    private final Set<User> loadedUser = new HashSet<>();

    public UserManager(@NotNull Box plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public User loadUser(@NotNull UUID uuid) {
        try {
            User user = plugin.getStorage().loadUser(uuid).join();
            loadedUser.add(user);
            return user;
        } catch (Throwable e) {
            throw new IllegalStateException("Could not load user:" + uuid.toString(), e);
        }
    }

    @NotNull
    public User getUser(@NotNull UUID uuid) {
        Optional<User> user = loadedUser.stream().filter(u -> u.getUuid().equals(uuid)).findFirst();
        return user.orElseGet(() -> loadUser(uuid));
    }

    public void saveUser(@NotNull User user) {
        try {
            plugin.getStorage().saveUser(user).join();
        } catch (Throwable e) {
            throw new IllegalStateException("Could not save user:" + user.getName(), e);
        }
    }

    public void unloadUser(@NotNull User user) {
        loadedUser.remove(user);
    }

    public boolean isLoaded(@NotNull UUID uuid) {
        return loadedUser.stream().anyMatch(u -> u.getUuid().equals(uuid));
    }

    @NotNull
    public UserCheckResult checkUser(@NotNull UUID uuid, @NotNull String name) {
        UserCheckResult checkResult;

        try {
            checkResult = plugin.getStorage().checkUser(uuid, name).join();
        } catch (Throwable e) {
            checkResult = UserCheckResult.EXCEPTION_OCCURS;
        }

        if (checkResult != UserCheckResult.NEW_PLAYER) {
            return checkResult;
        }

        try {
            plugin.getStorage().saveDefaultUserData(uuid).join();
            return checkResult;
        } catch (Throwable e) {
            return UserCheckResult.EXCEPTION_OCCURS;
        }
    }
}
