package net.okocraft.box.listeners;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.okocraft.box.Box;
import net.okocraft.box.config.Config;
import net.okocraft.box.database.ItemData;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.gui.CategorySelectorGUI;

public class BoxStick implements Listener {

    private final Box plugin = Box.getInstance();
    private final Config config = plugin.getAPI().getConfig();
    private final PlayerData playerData = plugin.getAPI().getPlayerData();
    private final ItemData itemData = plugin.getAPI().getItemData();
    private final NamespacedKey stickKey = new NamespacedKey(plugin, "boxstick");

    public void start() {
        HandlerList.unregisterAll(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof BoxStick)) {
            return false;
        }
        BoxStick boxStick = (BoxStick) o;
        return Objects.equals(plugin, boxStick.plugin) && Objects.equals(config, boxStick.config) && Objects.equals(playerData, boxStick.playerData) && Objects.equals(itemData, boxStick.itemData) && Objects.equals(stickKey, boxStick.stickKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plugin, config, playerData, itemData, stickKey);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractWithStick(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (!isBoxStick(event.getPlayer().getEquipment().getItemInMainHand())) {
            return;
        }

        if (config.getDisabledWorlds().contains(event.getPlayer().getWorld().getName())) {
            return;
        }

        event.getPlayer().openInventory(new CategorySelectorGUI().getInventory());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().hasPermission("box.stick.block")) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack handItem = event.getItemInHand();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() == mainHandItem.getType()) {
            if (useItemFromDatabase(handItem, player)) {
                mainHandItem.setAmount(mainHandItem.getAmount());
                player.getInventory().setItemInMainHand(mainHandItem);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void itemConsume(PlayerItemConsumeEvent event) {
        if (!event.getPlayer().hasPermission("box.stick.food")) {
            return;
        }
        if (useItemFromDatabase(event.getItem(), event.getPlayer())) {
            event.setItem(event.getItem().clone());
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void itemBreak(PlayerItemBreakEvent event) {
        if (!event.getPlayer().hasPermission("box.stick.tool")) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getBrokenItem();
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(0);
        }
        clone.setItemMeta(meta);
        if (useItemFromDatabase(clone, player)) {
            item.setAmount(2);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1F, 1F);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void potionThrow(ProjectileLaunchEvent event) {

        if (!(event.getEntity() instanceof ThrownPotion)) {
            return;
        }

        ThrownPotion thrownPotion = (ThrownPotion) event.getEntity();
        if (!(thrownPotion.getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) thrownPotion.getShooter();
        if (!player.hasPermission("box.stick.potion")) {
            return;
        }
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

        if (config.getDisabledWorlds().contains(player.getWorld().getName())) {
            return false;
        }

        if (!player.hasPermission("box.stick")) {
            return false;
        }

        PlayerInventory inv = player.getInventory();
        ItemStack offHandItem = inv.getItemInOffHand();
        if (offHandItem.getType() != Material.STICK) {
            return false;
        }

        if (!isBoxStick(offHandItem)) {
            return false;
        }

        ItemStack index = item.clone();
        index.setAmount(1);
        String itemName = itemData.getName(index);
        if (itemName == null || !plugin.getAPI().getCategories().getAllItems().contains(itemName)) {
            return false;
        }

        int stock = playerData.getStock(player, index);
        if (stock < 1) {
            return false;
        }

        playerData.setStock(player, index, stock - 1);
        return true;
    }

    private boolean isBoxStick(ItemStack item) {
        if (item == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        Integer data = meta.getPersistentDataContainer().get(stickKey, PersistentDataType.INTEGER);
        if (data == null || data != 1) {
            return false;
        }
        
        return true;
    }
}