package net.okocraft.box.feature.autostore.integration;

import net.coreprotect.listener.entity.EntityPickupItemListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class CoreProtectIntegration {

    public static void logItemPickup(@NotNull Player player, @NotNull Location location, @NotNull ItemStack item) {
        if (Bukkit.getPluginManager().getPlugin("CoreProtect") != null) {
            EntityPickupItemListener.onItemPickup(player, location, item);
        }
    }
}
