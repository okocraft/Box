package net.okocraft.box.feature.autostore.integration;

import net.okocraft.kansokusha.paper.builtin.PaperExternalAuditEvents;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class KansokushaIntegration {

    private static boolean forcedDisabled = false;

    public static void logItemPickup(@NotNull Player player, @NotNull Location location, @NotNull ItemStack item, @Nullable UUID itemEntityId) {
        if (forcedDisabled || !Bukkit.getPluginManager().isPluginEnabled("Kansokusha")) {
            return;
        }

        try {
            PaperExternalAuditEvents.recordItemPickup(player, location, item, itemEntityId);
        } catch (Throwable e) {
            forcedDisabled = true;
        }
    }
}
