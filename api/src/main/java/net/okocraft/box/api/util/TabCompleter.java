package net.okocraft.box.api.util;

import net.okocraft.box.api.BoxProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
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
        var itemNameFilter = filter.toUpperCase(Locale.ENGLISH);
        return BoxProvider.get()
                .getItemManager()
                .getItemNameList()
                .stream()
                .filter(itemName -> itemName.startsWith(itemNameFilter))
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
        var playerNameFilter = filter.toLowerCase(Locale.ENGLISH);

        return Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> permissionNode == null || player.hasPermission(permissionNode))
                .map(HumanEntity::getName)
                .filter(playerName -> playerName.toLowerCase(Locale.ENGLISH).startsWith(playerNameFilter))
                .collect(Collectors.toList());
    }
}
