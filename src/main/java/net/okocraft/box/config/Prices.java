package net.okocraft.box.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import net.okocraft.box.database.Items;

public final class Prices {

    private static CustomConfig prices = new CustomConfig("prices.yml");
    static {
        saveDefault();
    }

    /**
     * Cannot use constructor
     */
    private Prices() {
    }

    public static double getSellPrice(ItemStack item) {
        String name = Items.getName(item, true);
        return get().getDouble(name + ".sell");
    }
    
    public static double getBuyPrice(ItemStack item) {
        String name = Items.getName(item, true);
        return get().getDouble(name + ".buy");
    }

    /**
     * Reload config. If this method used before {@code JailConfig.save()}, the
     * data on memory will be lost.
     */
    public static void reload() {
        prices.initConfig();
    }

    /**
     * Saves data on memory to yaml.
     */
    public static void save() {
        prices.saveConfig();
    }

    /**
     * Copies yaml from jar to data folder.
     */
    public static void saveDefault() {
        prices.saveDefaultConfig();
    }

    /**
     * Gets FileConfiguration of config.
     * 
     * @return config.
     */
    static FileConfiguration get() {
        return prices.getConfig();
    }
}