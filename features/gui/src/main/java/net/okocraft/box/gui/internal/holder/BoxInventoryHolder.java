package net.okocraft.box.gui.internal.holder;

import net.okocraft.box.gui.api.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BoxInventoryHolder implements InventoryHolder {

    private final Inventory inventory;
    private final Menu menu;
    private final ItemStack[] icons;

    public BoxInventoryHolder(@NotNull Menu menu) {
        this.inventory = Bukkit.createInventory(this, menu.getRows() * 9, menu.getTitle());
        this.menu = menu;
        this.icons = new ItemStack[inventory.getSize()];
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void processClick(@NotNull Player clicker, int slot, @NotNull ClickType clickType) {
        menu.clickMenu(clicker, slot, clickType);
    }

    public void initializeMenu(@NotNull Player viewer) {
        menu.updateMenu(viewer);
        menu.applyIcons(icons);
    }

    public boolean updateMenu(@NotNull Player viewer) {
        if (menu.shouldUpdate()) {
            menu.updateMenu(viewer);
        }

        if (menu.isUpdated()) {
            menu.applyIcons(icons);
            return true;
        }

        return false;
    }

    public void applyContents() {
        inventory.setContents(icons);
    }

    public void updateInventory(@NotNull Player viewer) {
        applyContents();
        viewer.updateInventory();
    }

    public void onOpen(@NotNull Player viewer) {
        menu.onOpen(viewer);
    }
}
