package net.okocraft.box.feature.stick.function.container;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

class BoxStickInventoryView extends InventoryView {

    private final Player player;
    private final Inventory inventory;

    BoxStickInventoryView(@NotNull Player player, @NotNull Inventory inventory) {
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
