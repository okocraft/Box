package net.okocraft.box.plugin.gui.button;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.gui.BoxInventoryHolder;

public class BackMenuButton extends AbstractButton {
    
    private final BoxInventoryHolder previousMenu;
    
    public BackMenuButton(BoxInventoryHolder previousMenu) {
        super(new ButtonIcon(new ItemStack(Material.OAK_DOOR)));

        this.previousMenu = previousMenu;

        icon.applyConfig("back-menu");
        //TODO: ぷれほる
        icon.applyPlaceHolder(null);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e) {
        e.getWhoClicked().openInventory(previousMenu.getInventory());
    }

    @Override
    public void update() {
    }
}
