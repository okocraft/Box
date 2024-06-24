package net.okocraft.box.feature.stick.integration;

import net.coreprotect.listener.player.InventoryChangeListener;
import org.bukkit.Bukkit;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CoreProtectIntegration {

    private static boolean forcedDisabled = false;

    public static void logContainerTransaction(@NotNull Player player, @NotNull Container container) {
        if (forcedDisabled || !Bukkit.getPluginManager().isPluginEnabled("CoreProtect")) {
            return;
        }

        try {
            InventoryChangeListener.inventoryTransaction(player.getName(), container.getLocation(), null);
        } catch (Throwable e) {
            forcedDisabled = true;
        }
    }
}
