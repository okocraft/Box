package net.okocraft.box.listeners;

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
import org.jetbrains.annotations.Nullable;

import net.okocraft.box.Box;
import net.okocraft.box.config.Categories;
import net.okocraft.box.config.Config;
import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.gui.CategorySelectorGUI;

public class BoxStick implements Listener {

    @Nullable
    private static final Box plugin = Box.getInstance();
    private static final NamespacedKey stickKey = new NamespacedKey(plugin, "boxstick");

    public BoxStick() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteractWithStick(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (!isBoxStick(event.getPlayer().getEquipment().getItemInMainHand())) {
            return;
        }

        if (Config.getConfig().getDisabledWorlds().contains(event.getPlayer().getWorld())) {
            return;
        }

        event.getPlayer().openInventory(CategorySelectorGUI.GUI);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockPlace(BlockPlaceEvent event) {
        if (!Config.getBoxStickConfig().isEnabledBlockPlace()) {
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

    @EventHandler
    public void itemConsume(PlayerItemConsumeEvent event) {
        if (!Config.getBoxStickConfig().isEnabledFood()) {
            return;
        }
        if (useItemFromDatabase(event.getItem(), event.getPlayer())) {
            event.setItem(event.getItem().clone());
        }

    }

    @EventHandler
    public void itemBreak(PlayerItemBreakEvent event) {
        if (!Config.getBoxStickConfig().isEnabledTool()) {
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

    @EventHandler
    public void potionThrow(ProjectileLaunchEvent event) {
        if (!Config.getBoxStickConfig().isEnabledPotion()) {
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

        if (Config.getConfig().getDisabledWorlds().contains(player.getWorld())) {
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

        String itemName = Items.getName(item, false);
        if (itemName == null || !Categories.getInstance().getAllItems().contains(itemName)) {
            return false;
        }

        if (!item.getEnchantments().isEmpty()) {
            return false;
        }

        long stock = PlayerData.getItemAmount(player, item);
        if (stock < 1) {
            return false;
        }

        PlayerData.setItemAmount(player, item, stock - 1);
        return true;
    }

    public static boolean isBoxStick(ItemStack item) {
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