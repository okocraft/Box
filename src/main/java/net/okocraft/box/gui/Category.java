package net.okocraft.box.gui;

import java.util.List;
import java.util.Objects;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Category {

    @Nullable
    private static final Box INSTANCE = Box.getInstance();
    private static final NamespacedKey CATEGORY_NAME_KEY = new NamespacedKey(INSTANCE, "categoryname");

    @Getter
    private final String name;
    @NotNull
    @Getter
    private final String displayName;
    @NotNull
    @Getter
    private final ItemStack icon;
    @Getter
    private final List<ItemStack> items;

    public Category(String name, @NotNull String displayName, @NotNull ItemStack icon, @NotNull List<String> items) {
        this.name = name;
        this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        this.icon = icon;
        this.items = items.stream().filter(itemName -> Items.contains(itemName))
                .map(Items::getItemStack)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (icon.getType() != Material.AIR) {
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(this.displayName);
            meta.getPersistentDataContainer().set(CATEGORY_NAME_KEY, PersistentDataType.STRING, this.name);
            icon.setItemMeta(meta);
        }
    }

    @NotNull
    public ItemStack getIconItem() {
        return icon.clone();
    }
}