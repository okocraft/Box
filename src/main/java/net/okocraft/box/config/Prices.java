package net.okocraft.box.config;

import org.bukkit.inventory.ItemStack;

import net.okocraft.box.database.Items;


public final class Prices extends CustomConfig {

    public Prices() {
        super("prices.yml");
    }

    public double getSellPrice(ItemStack item) {
        String name = Items.getName(item, true);
        return get().getDouble(name + ".sell");
    }
    
    public double getBuyPrice(ItemStack item) {
        String name = Items.getName(item, true);
        return get().getDouble(name + ".buy");
    }
}