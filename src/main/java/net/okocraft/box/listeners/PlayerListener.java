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

public class PlayerListener implements Listener {
    
    private final Box plugin = Box.getInstance();
    private final Config config = plugin.getAPI().getConfig();
    private final Messages messages = plugin.getAPI().getMessages();

    public void start() {
        HandlerList.unregisterAll(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * {@code HandlerList.unregisterAll(this)}で、別のインスタンスでも同じクラスなら削除されるように再実装。
     * 
     * @param obj 比較するオブジェクト
     * 
     * @return オブジェクトが同じクラスならtrue。
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlayerListener;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof BaseGUI
                && config.getDisabledWorlds().contains(event.getPlayer().getWorld().getName())) {
            event.setCancelled(true);
            messages.sendDisabledWorld(event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }

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
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getAPI().getPlayerData().loadCache(event.getPlayer());
    }
}