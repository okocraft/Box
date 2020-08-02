package net.okocraft.box.plugin.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ItemBuilder {

    private Material material;
    private String displayName;
    private List<String> lore;
    private boolean isGlowing;
    private int amount;

    public ItemBuilder setMaterial(Material material) {
        this.material = material;

        return this;
    }

    public ItemBuilder setDisplayName(String displayName) {
        this.displayName = displayName;

        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.lore = lore;

        return this;
    }

    public ItemBuilder setGlowing(boolean glowing) {
        isGlowing = glowing;

        return this;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ItemStack build() {
        Objects.requireNonNull(material);

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (displayName != null) {
                meta.setDisplayName(Colorizer.colorize(displayName));
            } else {
                meta.setDisplayName(null);
            }

            if (lore != null) {
                meta.setLore(lore.stream().map(Colorizer::colorize).collect(Collectors.toList()));
            } else {
                meta.setLore(null);
            }

            if (isGlowing) {
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        }

        item.setItemMeta(meta);
        item.setAmount(0 < amount ? amount : 1);

        return item;
    }
}
