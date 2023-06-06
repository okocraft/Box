package net.okocraft.box.feature.craft.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Distribution {

    private static final Set<UUID> TO_INVENTORY = ConcurrentHashMap.newKeySet();

    public static boolean toInventory(@NotNull Player player) {
        return TO_INVENTORY.contains(player.getUniqueId());
    }

    public static void toggle(@NotNull Player player) {
        if (toInventory(player)) {
            TO_INVENTORY.remove(player.getUniqueId());
        } else {
            TO_INVENTORY.add(player.getUniqueId());
        }
    }
}
