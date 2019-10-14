package net.okocraft.box.listeners;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.okocraft.box.Box;
import net.okocraft.box.config.Categories;
import net.okocraft.box.config.Config;
import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import org.jetbrains.annotations.Nullable;

public class BoxStick implements Listener {

    @Nullable
    private static final Box plugin = Box.getInstance();
    private static final NamespacedKey stickKey = new NamespacedKey(plugin, "boxstick");
    private static ItemStack stick;

    static {
        initBoxStick();
    }

    public BoxStick() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * @return the box stick
     */
    public static ItemStack getStick() {
        return stick;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockPlace(BlockPlaceEvent event) {
        if (!Config.BoxStick.isEnabledBlockPlace()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack handItem = event.getItemInHand();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() == mainHandItem.getType()) {
            if (useItemFromDatabase(handItem, player)) {
                mainHandItem.setAmount(mainHandItem.getAmount() + 1);
                player.getInventory().setItemInMainHand(mainHandItem);
            }
        }
    }

    @EventHandler
    public void itemConsume(PlayerItemConsumeEvent event) {
        if (!Config.BoxStick.isEnabledFood()) {
            return;
        }
        if (useItemFromDatabase(event.getItem(), event.getPlayer())) {
            event.setItem(event.getItem().clone());
        }

    }

    @EventHandler
    public void itemBreak(PlayerItemBreakEvent event) {
        if (!Config.BoxStick.isEnabledTool()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getBrokenItem();
        if (useItemFromDatabase(new ItemStack(item.getType()), player)) {
            item.setAmount(2);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1F, 1F);
        }
    }

    @EventHandler
    public void potionThrow(ProjectileLaunchEvent event) {
        if (!Config.BoxStick.isEnabledPotion()) {
            return;
        }

        if (!(event.getEntity() instanceof ThrownPotion)) {
            return;
        }

        ThrownPotion thrownPotion = (ThrownPotion) event.getEntity();
        if (!(thrownPotion.getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) thrownPotion.getShooter();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null) {
            return;
        }

        Material handItemType = handItem.getType();
        if (handItemType != Material.SPLASH_POTION && handItemType != Material.LINGERING_POTION) {
            return;
        }

        if (useItemFromDatabase(handItem, player)) {
            handItem.setAmount(handItem.getAmount() + 1);
        }
    }

    private boolean useItemFromDatabase(ItemStack item, Player player) {

        if (player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        if (Config.getDisabledWorlds().contains(player.getWorld())) {
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
        if (itemName == null || !Categories.getAllItems().contains(itemName)) {
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
                        plugin.getConfig().getString("General.BoxStick.DisplayName", "&9Box Stick")));

                List<String> lore = plugin.getConfig().getStringList("General.BoxStick.Lore");
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