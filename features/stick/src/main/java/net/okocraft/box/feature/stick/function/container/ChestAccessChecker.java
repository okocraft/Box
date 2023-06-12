package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.feature.stick.integration.LWCIntegration;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public final class ChestAccessChecker {

    public static boolean canAccess(@NotNull Player player, @NotNull Container container, @NotNull ContainerOperation.OperationType operationType) {
        return LWCIntegration.canModifyInventory(player, container, operationType) &&
                !callInventoryOpenEvent(player, container.getInventory()).isCancelled(); // for WorldGuard (flag: chest-access)
    }

    private static @NotNull InventoryOpenEvent callInventoryOpenEvent(@NotNull Player player, @NotNull Inventory inventory) {
        var event = new InventoryOpenEvent(new BoxStickInventoryView(player, inventory));
        event.callEvent();
        return event;
    }
}
