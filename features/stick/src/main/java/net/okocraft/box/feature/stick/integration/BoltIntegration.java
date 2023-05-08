package net.okocraft.box.feature.stick.integration;

import net.okocraft.box.feature.stick.function.container.ContainerOperation;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.popcraft.bolt.BoltPlugin;
import org.popcraft.bolt.util.Permission;

final class BoltIntegration {

    static boolean canModify(@NotNull Player player, @NotNull BlockState state, @NotNull ContainerOperation.OperationType operationType) {
        var permission = switch (operationType) {
            case DEPOSIT -> Permission.WITHDRAW; // Withdraw items from the block and deposit them to Box
            case WITHDRAW -> Permission.DEPOSIT;  // Withdraw items from Box and deposit them to the block
        };

        return JavaPlugin.getPlugin(BoltPlugin.class).canAccess(state.getBlock(), player, permission);
    }

    private BoltIntegration() {
        throw new UnsupportedOperationException();
    }
}
