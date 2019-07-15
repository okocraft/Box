/*
 * Box
 * Copyright (C) 2019 OKOCRAFT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.okocraft.box.listeners;

import java.util.List;

import lombok.val;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.plugin.Plugin;

import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;

public class EntityPickupItem implements Listener {
    private Database database;
    private GeneralConfig config;

    private List<String> allItems;

    public EntityPickupItem(Database database, Plugin plugin) {
        // Register this event
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        // Initialize...
        this.config   = Box.getInstance().getGeneralConfig();
        this.database = database;
        allItems      = config.getAllItems();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (config.getDisabledWorlds().contains(event.getEntity().getWorld())) {
            return;
        }

        val pickedItemStack = event.getItem().getItemStack();
        if (pickedItemStack.hasItemMeta()) {
            return;
        }

        val itemName = pickedItemStack.getType().name();
        if (!allItems.contains(itemName)) {
            return;
        }

        val player = (Player) event.getEntity();
        if (!database.get("autostore_" + itemName, player.getUniqueId().toString()).equals("true")) {
            return;
        }

        int amount = event.getItem().getItemStack().getAmount();

        long currentItems;
        try{
            currentItems = Long.parseLong(database.get(itemName, player.getUniqueId().toString()));
        } catch (NumberFormatException exception) {
            currentItems = 0;
        }

        database.set(
                itemName,
                player.getUniqueId().toString(),
                String.valueOf(currentItems + amount)
        );

        event.getItem().remove();
        event.setCancelled(true);

        player.playSound(
                player.getLocation(),
                config.getTakeInSound(),
                config.getSoundPitch(),
                config.getSoundVolume()
        );
    }
}
