package net.okocraft.box.listeners;

import lombok.Getter;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;
import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.util.OtherUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Optional;

public class BoxStick implements Listener {

    private static final Box INSTANCE = Box.getInstance();
    private static final GeneralConfig CONFIG = INSTANCE.getGeneralConfig();
    private static final Database DATABASE = INSTANCE.getDatabase();
    private static final NamespacedKey stickKey = new NamespacedKey(INSTANCE, "boxstick");

    @Getter
    private static final ItemStack stick = new ItemStack(Material.STICK) {
        {
            ItemMeta meta = getItemMeta();
            String stickDisplayName = CONFIG.getDefaultConfig().getString("General.BoxStick.DisplayName", "&9Box Stick");
            if (meta != null && stickDisplayName != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        stickDisplayName));

                List<String> lore = CONFIG.getDefaultConfig().getStringList("General.BoxStick.Lore");
                if (lore.isEmpty()) {
                    lore.add("§r");
                    lore.add("§7利き手じゃない手にこれを持つと、利き手の");
                    lore.add("§7アイテムを使った時にBoxから消費します。");
                }
                lore.replaceAll(loreLine -> ChatColor.translateAlternateColorCodes('&', loreLine));
                meta.setLore(lore);

                meta.getPersistentDataContainer().set(stickKey, PersistentDataType.INTEGER, 1);
                setItemMeta(meta);
            }
        }
    };

    public BoxStick() {
        Bukkit.getPluginManager().registerEvents(this, INSTANCE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        useItemFromDatabase(event.getItemInHand(), event.getPlayer());
    }

    @EventHandler
    public void itemConsume(PlayerItemConsumeEvent event) {
        useItemFromDatabase(event.getItem(), event.getPlayer());
    }

    private void useItemFromDatabase(ItemStack item, Player player) {

        if (!CONFIG.getDefaultConfig().getBoolean("General.BoxStick.Enabled")) {
            return;
        }

        if (CONFIG.getDisabledWorlds().contains(player.getWorld().getName())) {
            return;
        }

        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (offHandItem.getType() != Material.STICK) {
            return;
        }

        if (offHandItem.getItemMeta() == null) return;

        if (Optional.ofNullable(
                offHandItem.getItemMeta().getPersistentDataContainer().get(stickKey, PersistentDataType.INTEGER))
                .orElse(0) != 1) {
            return;
        }

        ItemStack usedItem = item.clone();
        if (!CONFIG.getAllItems().contains(usedItem.getType().name())) {
            return;
        }

        int stock = OtherUtil.parseIntOrDefault(DATABASE.get(usedItem.getType().name(), player.getUniqueId().toString()), 0);
        if (stock < 1) {
            return;
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                player.getInventory().setItemInMainHand(usedItem);
                DATABASE.set(usedItem.getType().name(), player.getUniqueId().toString(), Integer.toString(stock - 1));
            }
        }.runTaskLater(INSTANCE, 1L);
    }
}