package net.okocraft.box.config;

import org.bukkit.inventory.ItemStack;

import net.okocraft.box.Box;
import net.okocraft.box.database.ItemData;



public final class Prices extends CustomConfig {

    private final ItemData itemData = Box.getInstance().getAPI().getItemData();

    public Prices() {
        super("prices.yml");
    }

    public double getSellPrice(ItemStack item) {
        item = item.clone();
        item.setAmount(1);
        String name = itemData.getName(item);
        return get().getDouble(name + ".sell");
    }
    
    public double getBuyPrice(ItemStack item) {
        item = item.clone();
        item.setAmount(1);
        String name = itemData.getName(item);
        return get().getDouble(name + ".buy");
    }
}