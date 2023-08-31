package net.okocraft.box.feature.gui.internal.holder;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BoxInventoryHolder implements InventoryHolder {

    private final Menu menu;
    private final int size;
    private final ItemStack[] icons;

    private Component savedTitle;
    private Inventory inventory;
    private boolean inventoryChanged = false;

    public BoxInventoryHolder(@NotNull Menu menu) {
        this.menu = menu;
        this.size = menu.getRows() * 9;
        this.icons = new ItemStack[size];
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void processClick(@NotNull Player clicker, int slot, @NotNull ClickType clickType) {
        menu.clickMenu(clicker, slot, clickType);
    }

    public void initializeMenu(@NotNull Player viewer) {
        savedTitle = menu.getTitle();
        inventory = Bukkit.createInventory(this, size, TranslationUtil.render(savedTitle, viewer));
        menu.updateMenu(viewer);
        menu.applyIcons(icons);
    }

    public boolean updateMenu(@NotNull Player viewer) {
        if (!savedTitle.equals(menu.getTitle())) {
            inventoryChanged = true;
            initializeMenu(viewer);
            applyContents();
            return true;
        }

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
        if (inventoryChanged) {
            viewer.openInventory(inventory);
            inventoryChanged = false;
        } else {
            applyContents();
            viewer.updateInventory();
        }
    }

    public @NotNull Menu getMenu() {
        return menu;
    }
}
