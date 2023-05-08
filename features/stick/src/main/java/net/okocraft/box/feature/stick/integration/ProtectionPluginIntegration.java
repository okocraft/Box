package net.okocraft.box.feature.stick.integration;

import net.okocraft.box.feature.stick.function.container.ContainerOperation;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ProtectionPluginIntegration {

    public static boolean canModify(@NotNull Player player, @NotNull BlockState state, @NotNull ContainerOperation.OperationType operationType) {
        if (isPluginEnabled("Bolt")) {
            return BoltIntegration.canModify(player, state, operationType);
        } else if (isPluginEnabled("LWC")) {
            return LWCIntegration.canModify(player, state, operationType);
        } else {
            return true;
        }
    }

    private static boolean isPluginEnabled(@NotNull String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    private ProtectionPluginIntegration() {
        throw new UnsupportedOperationException();
    }
}
