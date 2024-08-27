package net.okocraft.box.feature.stick.integration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldGuardIntegration {

    public static boolean canModify(@NotNull Player player, @NotNull Location location) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer()
            .createQuery()
            .testBuild(BukkitAdapter.adapt(location), WorldGuardPlugin.inst().wrapPlayer(player), Flags.CHEST_ACCESS);
    }

    private WorldGuardIntegration() {
    }
}
