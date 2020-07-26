package net.okocraft.box.plugin.model.stick;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.config.GeneralConfig;
import net.okocraft.box.plugin.util.Colorizer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class BoxStick {

    private final ItemStack item;

    private BoxStick(@NotNull ItemStack item) {
        this.item = item;
    }

    public boolean isStick(@NotNull ItemStack other) {
        return item.isSimilar(other);
    }

    public boolean give(@NotNull Player player) {
        return player.getInventory().addItem(item).isEmpty();
    }

    @Contract("_ -> new")
    @NotNull
    public static BoxStick create(@NotNull Box plugin) {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            plugin.getLogger().warning("Could not get item meta: " + item.getType().toString());
            plugin.getLogger().warning("Continue with the default stick...");
        } else {
            GeneralConfig config = plugin.getGeneralConfig();
            meta.setDisplayName(Colorizer.colorize(config.getStickName()));
            meta.setLore(config.getStickLore().stream().map(Colorizer::colorize).collect(Collectors.toList()));

            if (config.isStickGlowed()) {
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return new BoxStick(item);
    }
}
