package net.okocraft.box.feature.command.util;

import net.okocraft.box.api.BoxProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class TabCompleter {

    public static @NotNull List<String> itemNames(@NotNull String filter) {
        var itemNameFilter = filter.toUpperCase(Locale.ROOT);
        return BoxProvider.get()
                .getItemManager()
                .getItemNameSet()
                .stream()
                .filter(itemName -> itemName.startsWith(itemNameFilter))
                .sorted()
                .collect(Collectors.toList());
    }

    public static @NotNull List<String> players(@NotNull String filter) {
        var playerNameFilter = filter.toLowerCase(Locale.ROOT);

        return Bukkit.getOnlinePlayers()
                .stream()
                .map(HumanEntity::getName)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .filter(playerName -> playerName.startsWith(playerNameFilter))
                .sorted()
                .collect(Collectors.toList());
    }

    public static @NotNull List<String> players(@NotNull String filter, @NotNull String permissionNode) {
        var playerNameFilter = filter.toLowerCase(Locale.ROOT);

        return Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> player.hasPermission(permissionNode))
                .map(HumanEntity::getName)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .filter(playerName -> playerName.startsWith(playerNameFilter))
                .sorted()
                .collect(Collectors.toList());
    }
}
