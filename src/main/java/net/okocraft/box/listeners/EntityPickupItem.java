package net.okocraft.box.listeners;

import java.util.List;

import net.okocraft.box.ConfigManager;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class EntityPickupItem implements Listener {

    private Database database;
    private ConfigManager configManager;

    private List<String> allItems;

    public EntityPickupItem(Database database, Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.database = database;
        configManager = Box.getInstance().getConfigManager();
        allItems = configManager.getAllItems();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(EntityPickupItemEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        ItemStack pickedItemStack = event.getItem().getItemStack();
        if (pickedItemStack.hasItemMeta()) return;

        String itemName = pickedItemStack.getType().name();
        if (!allItems.contains(itemName)) return;

        Player player = (Player) event.getEntity();
        if (!database.get("autostore_" + itemName, player.getUniqueId().toString()).equals("true")) return;

        int amount = event.getItem().getItemStack().getAmount();

        event.getItem().remove();
        event.setCancelled(true);

        long currentItems;
        try{
            currentItems = Long.parseLong(database.get(itemName, player.getUniqueId().toString()));
        } catch (NumberFormatException exception) {
            currentItems = 0;
        }

        database.set(itemName, player.getUniqueId().toString(), String.valueOf(currentItems + amount));
        event.getItem().remove();
        event.setCancelled(true);
        player.playSound(player.getLocation(), configManager.getTakeInSound(), configManager.getSoundPitch(), configManager.getSoundVolume());
    }
}
