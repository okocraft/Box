package net.okocraft.box.plugin.gui.item;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface MenuItem {

    @NotNull
    ItemStack getItem();

    int getIndex();

    default void click(@NotNull InventoryClickEvent e) {
        if (!e.isCancelled()) {
            e.setCancelled(true);
        }

        onClick(e);
    }

    default void onClick(@NotNull InventoryClickEvent e) {
    }

    default void update() {
    }

    default void applyItem(@NotNull Inventory inv) {
        if (0 < getIndex() && getIndex() < inv.getSize()) {
            inv.setItem(getIndex(), getItem());
        }
    }
}
