package net.okocraft.box.gui;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.okocraft.box.Box;
import net.okocraft.box.database.Items;

public class Category {

    private static final Box INSTANCE = Box.getInstance();
    private static final NamespacedKey CATEGORY_NAME_KEY = new NamespacedKey(INSTANCE, "categoryname");

    @Getter
    private final String name;
    @Getter
    private final String displayName;
    @Getter
    private final Material iconMaterial;
    @Getter
    private final List<Items> items;
    private final ItemStack iconItem;

    public Category(String name, String displayName, Material icon, List<String> items) {
        this.name = name;
        this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        this.iconMaterial = icon;
        this.items = items.stream().filter(itemName -> Items.contains(itemName))
                .map(itemName -> Items.valueOf(itemName)).collect(Collectors.toList());

        this.iconItem = new ItemStack(icon);
        if (iconMaterial != Material.AIR) {
            ItemMeta meta = iconItem.getItemMeta();
            meta.setDisplayName(this.displayName);
            meta.getPersistentDataContainer().set(CATEGORY_NAME_KEY, PersistentDataType.STRING, this.name);
            iconItem.setItemMeta(meta);
        }
    }

    public ItemStack getIconItem() {
        return iconItem.clone();
    }
}