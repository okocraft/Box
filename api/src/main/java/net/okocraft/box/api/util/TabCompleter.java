package net.okocraft.box.api.util;

import net.okocraft.box.api.BoxAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class for tab completions.
 */
public final class TabCompleter {

    /**
     * Gets the set of item names that match the filter.
     *
     * @param filter the item name filter
     * @return the list of item names that match the filter
     */
    public static @NotNull List<String> itemNames(@NotNull String filter) {
        return BoxAPI.api()
                .getItemManager()
                .getItemNameList()
                .stream()
                .filter(itemName -> startsWith(itemName, filter))
                .collect(Collectors.toList());
    }

    /**
     * Gets the set of player names that match the filter.
     *
     * @param filter the player name filter
     * @return the list of player names that match the filter
     */
    public static @NotNull List<String> players(@NotNull String filter) {
        return players(filter, null);
    }

    /**
     * Gets the set of player names that match the filter.
     *
     * @param filter         the player name filter
     * @param permissionNode the permission node to check
     * @return the list of player names that match the filter
     */
    public static @NotNull List<String> players(@NotNull String filter, @Nullable String permissionNode) {
        return Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> permissionNode == null || player.hasPermission(permissionNode))
                .map(HumanEntity::getName)
                .filter(playerName -> startsWith(playerName, filter))
                .collect(Collectors.toList());
    }

    @VisibleForTesting
    static boolean startsWith(@NotNull String str, @NotNull String prefix) {
        if (prefix.isEmpty()) {
            return true;
        }

        int prefixLength = prefix.length();

        if (str.length() < prefixLength) {
            return false;
        }

        return str.regionMatches(true, 0, prefix, 0, prefixLength);
    }

    private TabCompleter() {
        throw new UnsupportedOperationException();
    }
}
