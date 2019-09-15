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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import net.okocraft.box.Box;
import net.okocraft.box.database.Items;
import net.okocraft.box.util.GeneralConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenerateItemConfig implements Listener {

    @Nullable
    private static final Box INSTANCE = Box.getInstance();
    private static final GeneralConfig CONFIG = INSTANCE.getGeneralConfig();

    private final Player player;
    private final String category;
    private final String id;
    private final String displayName;
    private final Material icon;

    public GenerateItemConfig(Player player, String category, String id, String displayName, @NotNull String icon) {
        Bukkit.getPluginManager().registerEvents(this, INSTANCE);
        this.player = player;
        this.category = category;
        this.id = id;
        this.displayName = displayName;
        this.icon = Optional.ofNullable(Material.getMaterial(icon)).orElse(Material.AIR);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChestClick(@NotNull PlayerInteractEvent event) {
        if (event.getPlayer() != player) {
            return;
        }

        Block chest = event.getClickedBlock();

        if (chest == null || event.getClickedBlock().getType() != Material.CHEST) {
            event.setCancelled(true);
            player.sendMessage("ブロックがチェストではなかったため、キャンセルされました。");
            HandlerList.unregisterAll(this);
            return;
        }

        HandlerList.unregisterAll(this);
        event.setCancelled(true);

        File file = INSTANCE.getDataFolder().toPath().resolve(category + ".yml").toFile();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                player.sendMessage("ファイルを作ることができなかったため、キャンセルされました。");
                return;
            }
        }

        Chest chestData = (Chest) chest.getState();
        Inventory chestSnapInv = chestData.getSnapshotInventory();

        FileConfiguration itemConfig = CONFIG.getItemConfig();
        String categoryPath = "categories." + category;
        itemConfig.set(categoryPath + ".id", id);
        itemConfig.set(categoryPath + ".display_name", displayName);
        itemConfig.set(categoryPath + ".icon", icon.name());
        
        List<String> items = Arrays.stream(chestSnapInv.getContents())
                .filter(itemStack -> itemStack != null)
                .map(itemStack -> Items.getName(itemStack, false))
                .collect(Collectors.toList());
        
        itemConfig.set(categoryPath + ".item", items);

        CONFIG.getItemCustomConfig().saveConfig();
        CONFIG.addCategory();

        player.sendMessage("新たなカテゴリの追加が完了しました。");
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        if (event.getPlayer() == player) {
            HandlerList.unregisterAll(this);
        }
    }
}