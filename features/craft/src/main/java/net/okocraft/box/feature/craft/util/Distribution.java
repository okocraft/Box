package net.okocraft.box.feature.craft.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Distribution {

    private static final List<UUID> TO_INVENTORY = new ArrayList<>();

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
