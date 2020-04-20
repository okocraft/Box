package net.okocraft.box.config;

import org.bukkit.inventory.ItemStack;

import net.okocraft.box.Box;



public final class Prices extends CustomConfig {

    public Prices() {
        super("prices.yml");
    }

    public double getSellPrice(ItemStack item) {
        item = item.clone();
        item.setAmount(1);
        String name = Box.getInstance().getAPI().getItemData().getName(item);
        return get().getDouble(name + ".sell");
    }
    
    public double getBuyPrice(ItemStack item) {
        item = item.clone();
        item.setAmount(1);
        String name = Box.getInstance().getAPI().getItemData().getName(item);
        return get().getDouble(name + ".buy");
    }
}