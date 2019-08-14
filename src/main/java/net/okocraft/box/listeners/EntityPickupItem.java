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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.val;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;

public class EntityPickupItem implements Listener {
    private final Database database;
    private final GeneralConfig config;

    private final Map<Player, Long> cooldown = new HashMap<>();
    private final Map<Player, Map<Material, Integer>> playerItemMap = new HashMap<>();

    private final Map<Player, Map<String, String>> autoStore = new HashMap<>();

    public EntityPickupItem(Database database, Plugin plugin) {
        // Register this event
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        // Initialize...
        this.config   = Box.getInstance().getGeneralConfig();
        this.database = database;
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

        ItemStack pickedItem = event.getItem().getItemStack();
        if (pickedItem.hasItemMeta()) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!autoStore.containsKey(player)) {
            autoStore.put(player, new HashMap<>());
        }
        
        Material pickedMaterial = pickedItem.getType();
        val itemName = pickedMaterial.name();
        if (!autoStore.get(player).containsKey(itemName)) {
            String value = database.get("autostore_" + itemName, player.getUniqueId().toString());
            autoStore.get(player).put(itemName, value);
        }

        if (!autoStore.get(player).get(itemName).equalsIgnoreCase("true")) {
            return;
        }

        Map<Material, Integer> items = playerItemMap.getOrDefault(player, new HashMap<>());
        int newAmount = pickedItem.getAmount() + items.getOrDefault(pickedMaterial, 0);
        items.put(pickedMaterial, newAmount);
        playerItemMap.put(player, items);

        event.getItem().remove();
        event.setCancelled(true);

        player.playSound(
            player.getLocation(),
            config.getTakeInSound(),
            config.getSoundPitch(),
            config.getSoundVolume()
        );

        if (!cooldown.containsKey(player)) {
            cooldown.put(player, System.currentTimeMillis() + 1000L);
            
            new BukkitRunnable(){
                
                @Override
                public void run() {
                    if (cooldown.get(player) < System.currentTimeMillis()) {
                        commit(player);
                        cooldown.remove(player);
                        autoStore.remove(player);
                        cancel();
                    }
                }
            }.runTaskTimer(Box.getInstance(), 20L, 20L);
        } else {
            cooldown.put(player, System.currentTimeMillis() + 1000L);
        }
    }

    private void commit(Player player) {
        Map<Material, Integer> playerItem = playerItemMap.get(player);
        if (playerItem == null) {
            return;
        }

        Map<String, String> oldValues = database.getMultiValue(
                playerItem.keySet().parallelStream().map(Enum::name).collect(Collectors.toList()),
                player.getName()
        );

        Map<String, String> newValues = new HashMap<>();
        oldValues.forEach((item, amountString) -> {
            String materialString = item.toUpperCase();
            Material material;
            int amount;
            try {
                material = Material.valueOf(materialString);
                amount = Integer.parseInt(amountString);
            } catch (IllegalArgumentException e) {
                return;
            }

            newValues.put(materialString, String.valueOf(amount + playerItem.get(material)));
        });

        database.setMultiValue(newValues, player.getUniqueId().toString());
        playerItemMap.remove(player);
    }
}
