package net.okocraft.box.plugin.gui.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EmptyItem extends AbstractMenuItem {

    private final static ItemStack AIR = new ItemStack(Material.AIR);

    public EmptyItem(int index) {
        super(AIR, index);
    }
}
