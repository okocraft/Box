package net.okocraft.box.plugin.model.manager;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.result.UserCheckResult;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UserManager {

    private final Box plugin;

    public UserManager(@NotNull Box plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public User loadUser(@NotNull UUID uuid) {
        try {
            return plugin.getStorage().loadUser(uuid).join();
        } catch (Throwable e) {
            throw new IllegalStateException("Could not load user:" + uuid.toString(), e);
        }
    }

    public void saveUser(@NotNull User user) {
        try {
            plugin.getStorage().saveUser(user).join();
        } catch (Throwable e) {
            throw new IllegalStateException("Could not save user:" + user.getName(), e);
        }
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
