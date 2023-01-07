package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.feature.stick.integration.LWCIntegration;
import org.bukkit.block.Container;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

public final class ChestAccessChecker {

    public static boolean canAccess(@NotNull Player player, @NotNull Container container, @NotNull ContainerOperation.OperationType operationType) {
        return LWCIntegration.canModifyInventory(player, container, operationType) &&
                !callInventoryOpenEvent(player, container.getInventory()).isCancelled(); // for WorldGuard (flag: chest-access)
    }

    private static @NotNull InventoryOpenEvent callInventoryOpenEvent(@NotNull Player player, @NotNull Inventory inventory) {
        var event = new InventoryOpenEvent(new DummyInventoryView(player, inventory));
        event.callEvent();
        return event;
    }

    private static class DummyInventoryView extends InventoryView {

        private final Player player;
        private final Inventory inventory;

        private DummyInventoryView(@NotNull Player player, @NotNull Inventory inventory) {
            this.player = player;
            this.inventory = inventory;
        }

        @Override
        public @NotNull Inventory getTopInventory() {
            return inventory;
        }

        @Override
        public @NotNull Inventory getBottomInventory() {
            return player.getInventory();
        }

        @Override
        public @NotNull HumanEntity getPlayer() {
            return player;
        }

        @Override
        public @NotNull InventoryType getType() {
            return inventory.getType();
        }

        @Override
        public @NotNull String getTitle() {
            return "";
        }
    }
}
