package net.okocraft.box.api.util;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.user.BoxUser;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class UserSearcher {

    public static @Nullable BoxUser search(@NotNull String uuidOrName) {
        var uuid = toUuidOrNull(uuidOrName);

        var onlinePlayer = uuid != null ? Bukkit.getPlayer(uuid) : Bukkit.getPlayer(uuidOrName);
        var playerMap = BoxProvider.get().getBoxPlayerMap();

        if (onlinePlayer != null && playerMap.isLoaded(onlinePlayer)) {
            return playerMap.get(onlinePlayer).asUser();
        }

        var userManager = BoxProvider.get().getUserManager();
        return uuid != null ? userManager.loadUser(uuid).join() : userManager.search(uuidOrName).join().orElse(null);
    }

    private static @Nullable UUID toUuidOrNull(@NotNull String strUuid) {
        try {
            return UUID.fromString(strUuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
