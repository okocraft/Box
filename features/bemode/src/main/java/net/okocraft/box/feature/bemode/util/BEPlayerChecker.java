package net.okocraft.box.feature.bemode.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BEPlayerChecker {

    public static boolean isBEPlayer(@NotNull Player player) {
        return player.getUniqueId().toString().startsWith("00000000");
    }
}
