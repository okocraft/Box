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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import net.okocraft.box.Box;
import net.okocraft.box.config.Categories;
import net.okocraft.box.config.Config;
import net.okocraft.box.database.ItemData;
import net.okocraft.box.database.PlayerData;

public class EntityPickupItem implements Listener {

    private final Box plugin = Box.getInstance();
    private final Config config = plugin.getAPI().getConfig();
    private final Categories categories = plugin.getAPI().getCategories();
    private final PlayerData playerData = plugin.getAPI().getPlayerData();
    private final ItemData itemData = plugin.getAPI().getItemData();

    public EntityPickupItem(Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!config.isAutoStoreEnabled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (config.getDisabledWorlds().contains(event.getEntity().getWorld().getName())) {
            return;
        }

        Player player = (Player) event.getEntity();
        ItemStack pickedItem = event.getItem().getItemStack();

        String pickedItemName = itemData.getName(pickedItem);

        if (!categories.getAllItems().contains(pickedItemName)) {
            return;
        }

        if (!playerData.getAutoStore(player, pickedItem)) {
            return;
        }

        int stock = playerData.getStock(player, pickedItem);
        playerData.setStock(player, pickedItem, stock + event.getItem().getItemStack().getAmount());

        event.getItem().remove();
        event.setCancelled(true);
        config.playDepositSound(player);
    }
}
