package net.okocraft.box.feature.stick.integration;

import net.okocraft.kansokusha.paper.builtin.PaperExternalAuditEvents;
import org.bukkit.Bukkit;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class KansokushaIntegration {

    private static final String ACTION = "box_stick";

    private static boolean forcedDisabled = false;

    public static @Nullable ItemStack @Nullable [] snapshot(@NotNull Container container) {
        if (isUnavailable()) {
            return null;
        }

        try {
            return PaperExternalAuditEvents.snapshotContents(container.getInventory());
        } catch (Throwable e) {
            forcedDisabled = true;
            return null;
        }
    }

    public static void logContainerTransaction(@NotNull Player player, @NotNull Container container, @Nullable ItemStack @Nullable [] before) {
        if (before == null || isUnavailable()) {
            return;
        }

        try {
            PaperExternalAuditEvents.recordContainerTransaction(player, container.getInventory(), before, ACTION);
        } catch (Throwable e) {
            forcedDisabled = true;
        }
    }

    private static boolean isUnavailable() {
        return forcedDisabled || !Bukkit.getPluginManager().isPluginEnabled("Kansokusha");
    }
}
