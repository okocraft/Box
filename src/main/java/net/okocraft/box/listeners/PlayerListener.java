package net.okocraft.box.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import net.okocraft.box.Box;
import net.okocraft.box.config.Config;
import net.okocraft.box.config.Messages;
import net.okocraft.box.gui.BaseGUI;
import net.okocraft.box.gui.Clickable;
import net.okocraft.box.gui.GUICache;

public class PlayerListener implements Listener {
    
    private final Box plugin = Box.getInstance();
    private final Config config = plugin.getAPI().getConfig();
    private final Messages messages = plugin.getAPI().getMessages();

    public void start() {
        HandlerList.unregisterAll(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PlayerListener)) {
            return false;
        }
        PlayerListener playerListener = (PlayerListener) o;
        return Objects.equals(plugin, playerListener.plugin) && Objects.equals(config, playerListener.config) && Objects.equals(messages, playerListener.messages) && Objects.equals(categories, playerListener.categories) && Objects.equals(playerData, playerListener.playerData) && Objects.equals(itemData, playerListener.itemData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plugin, config, messages, categories, playerData, itemData);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof BaseGUI
                && config.getDisabledWorlds().contains(event.getPlayer().getWorld().getName())) {
            event.setCancelled(true);
            messages.sendDisabledWorld(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getView().getTopInventory();
        if (inv == null || !(inv.getHolder() instanceof BaseGUI)) {
            return;
        }

        event.setCancelled(true);

        if (inv.getType() == InventoryType.PLAYER || event.getCurrentItem() == null || event.getAction() == InventoryAction.NOTHING) {
            return;
        }

        if (config.getDisabledWorlds().contains(event.getWhoClicked().getWorld().getName())) {
            messages.sendDisabledWorld(event.getWhoClicked());
            return;
        }
        
        if (inv.getHolder() instanceof Clickable) {
            ((Clickable) inv.getHolder()).onClicked(event);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getAPI().getPlayerData().removeCache(event.getPlayer());
        GUICache.removeCache(event.getPlayer());
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getAPI().getPlayerData().loadCache(event.getPlayer());
    }
}