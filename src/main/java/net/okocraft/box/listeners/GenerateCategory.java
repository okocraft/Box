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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import net.okocraft.box.Box;
import net.okocraft.box.config.Categories;
import net.okocraft.box.config.Messages;
import net.okocraft.box.database.Items;

public class GenerateCategory implements Listener {

    private final Box plugin = Box.getInstance();
    private final Messages messages = plugin.getAPI().getMessages();

    private final Player player;
    private final String id;
    private final String displayName;
    private final String icon;

    public GenerateCategory(Player player, String id, String displayName, String icon) {
        this.player = player;
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        if (!Items.contains(icon)) {
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChestClick(PlayerInteractEvent event) {
        if (event.getPlayer() != player) {
            return;
        }

        Block chest = event.getClickedBlock();

        if (chest == null || event.getClickedBlock().getType() != Material.CHEST) {
            event.setCancelled(true);
            messages.sendMessage(player, "command.box-admin.add-category.error.not-chest-and-cancelled");
            HandlerList.unregisterAll(this);
            return;
        }

        HandlerList.unregisterAll(this);
        event.setCancelled(true);

        Chest chestData = (Chest) chest.getState();
        Inventory chestSnapInv = chestData.getSnapshotInventory();
        List<String> items = Arrays.stream(chestSnapInv.getContents())
                .filter(Objects::nonNull)
                .map(itemStack -> Items.getName(itemStack, false))
                .collect(Collectors.toList());
        Categories.getInstance().addCategory(id, displayName, items, icon);
        messages.sendMessage(player, "command.box-admin.add-category.info.success");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer() == player) {
            HandlerList.unregisterAll(this);
        }
    }
}