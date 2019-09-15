package net.okocraft.box.listeners;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import net.okocraft.box.Box;
import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.util.PlayerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoxStick implements Listener {

    @Nullable
    private static final Box INSTANCE = Box.getInstance();
    private static final GeneralConfig CONFIG = INSTANCE.getGeneralConfig();
    private static final NamespacedKey stickKey = new NamespacedKey(INSTANCE, "boxstick");

    @Getter
    private static ItemStack stick;
    static {
        initBoxStick();
    }

    public BoxStick() {
        Bukkit.getPluginManager().registerEvents(this, INSTANCE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockPlace(@NotNull BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = event.getItemInHand();
        if (useItemFromDatabase(handItem, player)) {
            PlayerInventory inv = player.getInventory();
            ItemStack clone = handItem.clone();
            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack mainHandItem = inv.getItemInMainHand();
                    if (mainHandItem.equals(handItem)) {
                        if (mainHandItem.getAmount() == mainHandItem.getMaxStackSize()) {
                            clone.setAmount(1);
                            if (inv.addItem(clone).containsValue(clone)) {
                                PlayerData.addItemAmount(player, handItem, 1);
                            }
                        } else {
                            inv.setItemInMainHand(clone);
                        }
                        return;
                    }

                    ItemStack offHandItem = inv.getItemInOffHand();
                    if (offHandItem.equals(handItem)) {
                        if (offHandItem.getAmount() == offHandItem.getMaxStackSize()) {
                            if (inv.addItem(clone).containsValue(clone)) {
                                PlayerData.addItemAmount(player, handItem, 1);
                            }
                        } else {
                            inv.setItemInOffHand(clone);
                        }
                        return;
                    }

                    if (inv.addItem(clone).containsValue(clone)) {
                        PlayerData.addItemAmount(player, handItem, 1);
                    }
                }
            }.runTaskLater(INSTANCE, 1L);

        }
    }

    @EventHandler
    public void itemConsume(@NotNull PlayerItemConsumeEvent event) {
        if (useItemFromDatabase(event.getItem(), event.getPlayer())) {
            event.setItem(event.getItem().clone());
        }

    }

    @EventHandler
    public void itemBreak(PlayerItemBreakEvent event) {
    }

    @EventHandler
    public void potionThrow(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack handItem = event.getItem();
        if (handItem == null) {
            return;
        }

        Material handItemType = handItem.getType();
        if (handItemType != Material.SPLASH_POTION && handItemType != Material.LINGERING_POTION) {
            return;
        }

        Player player = event.getPlayer();
        if (useItemFromDatabase(handItem, player)) {
            event.setCancelled(true);

            ThrownPotion thrownPotion = (ThrownPotion) player.getWorld()
                    .spawnEntity(player.getLocation().add(0, 1.5, 0), EntityType.SPLASH_POTION);
            thrownPotion.setItem(handItem.clone());
            thrownPotion.setVelocity(player.getLocation().getDirection().multiply(0.5));

            if (handItemType == Material.SPLASH_POTION) {
                PlayerUtil.playSound(player, Sound.ENTITY_SPLASH_POTION_THROW);
            } else {
                PlayerUtil.playSound(player, Sound.ENTITY_LINGERING_POTION_THROW);
            }

        }
    }

    private boolean useItemFromDatabase(@NotNull ItemStack item, @NotNull Player player) {

        if (player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        if (!INSTANCE.getConfig().getBoolean("General.BoxStick.Enabled")) {
            return false;
        }

        if (CONFIG.getDisabledWorlds().contains(player.getWorld().getName())) {
            return false;
        }

        PlayerInventory inv = player.getInventory();
        ItemStack offHandItem = inv.getItemInOffHand();
        if (offHandItem.getType() != Material.STICK) {
            return false;
        }

        if (Optional.ofNullable(
                offHandItem.getItemMeta().getPersistentDataContainer().get(stickKey, PersistentDataType.INTEGER))
                .orElse(0) != 1) {
            return false;
        }

        String itemName = Items.getName(item, false);
        if (itemName == null || !CONFIG.getAllItems().contains(itemName)) {
            return false;
        }

        long stock = PlayerData.getItemAmount(player, item);
        if (stock < 1) {
            return false;
        }

        PlayerData.setItemAmount(player, item, stock - 1);
        return true;
    }

    public static void initBoxStick() {
        stick = new ItemStack(Material.STICK) {
            {
                ItemMeta meta = getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        INSTANCE.getConfig().getString("General.BoxStick.DisplayName", "&9Box Stick")));

                List<String> lore = INSTANCE.getConfig().getStringList("General.BoxStick.Lore");
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
        };
    }
}