package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.feature.stick.integration.ProtectionPluginIntegration;
import net.okocraft.box.feature.stick.integration.WorldGuardIntegration;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ChestAccessChecker {

    public static boolean canAccess(@NotNull Player player, @NotNull Container container, @NotNull ContainerOperation.OperationType operationType) {
        return ProtectionPluginIntegration.canModify(player, container, operationType) && WorldGuardIntegration.canModify(player, container.getLocation());
    }
}
