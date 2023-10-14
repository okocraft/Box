package net.okocraft.box.api.util;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.user.BoxUser;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A utility class for searching for {@link BoxUser} from UUID or name.
 */
public final class UserSearcher {

    /**
     * Searches for {@link BoxUser} from UUID or name.
     *
     * @param uuidOrName UUID or username of the user
     * @return a search result
     */
    public static @Nullable BoxUser search(@NotNull String uuidOrName) {
        var uuid = toUuidOrNull(uuidOrName);

        var onlinePlayer = uuid != null ? Bukkit.getPlayer(uuid) : Bukkit.getPlayer(uuidOrName);
        var userManager = BoxProvider.get().getUserManager();

        if (onlinePlayer != null) {
            return userManager.createBoxUser(onlinePlayer.getUniqueId(), onlinePlayer.getName());
        } else {
            return uuid != null ? userManager.loadBoxUser(uuid) : userManager.searchByName(uuidOrName);
        }
    }

    private static @Nullable UUID toUuidOrNull(@NotNull String strUuid) {
        try {
            return UUID.fromString(strUuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
