package net.okocraft.box.plugin.gui;

import net.okocraft.box.plugin.Box;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class BoxInventoryHolder implements InventoryHolder {

    protected final Box plugin;
    protected final Inventory inv;
    protected final MenuType type;
    protected final ItemList itemList;

    public BoxInventoryHolder(@NotNull Box plugin, @NotNull MenuType type, @NotNull String title) {
        this.plugin = plugin;
        this.type = type;
        this.inv = plugin.getServer().createInventory(this, type.getSize(), title);
        this.itemList = new ItemList();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }

    @NotNull
    public MenuType getType() {
        return type;
    }

    public void openInventory(@NotNull HumanEntity player) {
        player.openInventory(inv);
    }

    public void updateInventory() {
        itemList.apply(inv);
    }

    public void onClick(@NotNull InventoryClickEvent e) {
        itemList.click(e);
    }

    public abstract void update();
}
