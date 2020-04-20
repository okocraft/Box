package net.okocraft.box.config;

import org.bukkit.inventory.ItemStack;

import net.okocraft.box.Box;



public final class Prices extends CustomConfig {

    public Prices() {
        super("prices.yml");
    }

    public double getSellPrice(ItemStack item) {
        String name = Box.getInstance().getAPI().getItemData().getName(item);
        return get().getDouble(name + ".sell");
    }
    
    public double getBuyPrice(ItemStack item) {
        String name = Box.getInstance().getAPI().getItemData().getName(item);
        return get().getDouble(name + ".buy");
    }
}