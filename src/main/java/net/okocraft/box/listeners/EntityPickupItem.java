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

import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.util.PlayerUtil;
import net.okocraft.box.Box;
import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import org.jetbrains.annotations.NotNull;

public class EntityPickupItem implements Listener {
    private final GeneralConfig config;

    public EntityPickupItem(@NotNull Plugin plugin) {
        // Register this event
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        // Initialize...
        this.config = Box.getInstance().getGeneralConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickupItem(@NotNull EntityPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

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

        String pickedItemName = Items.getName(pickedItem, false);

        if (!Box.getInstance().getGeneralConfig().getAllItems().contains(pickedItemName)) {
            return;
        }

        if (!PlayerData.getAutoStore(player, pickedItem)) {
            return;
        }

        long stock = PlayerData.getItemAmount(player, pickedItem);
        PlayerData.setItemAmount(player, pickedItem, stock + event.getItem().getItemStack().getAmount());

        event.getItem().remove();
        event.setCancelled(true);

        PlayerUtil.playSound(player, config.getTakeInSound());
    }
}
