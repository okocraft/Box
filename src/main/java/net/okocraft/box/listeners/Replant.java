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
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.okocraft.box.Box;
import net.okocraft.box.config.Config;
import net.okocraft.box.database.PlayerData;

public class Replant implements Listener {

    private final Box plugin = Box.getInstance();
    private final Config config = plugin.getAPI().getConfig();
    private final PlayerData playerData = plugin.getAPI().getPlayerData();

    private static final Map<Material, Material> PLANTS = new HashMap<>() {
        private static final long serialVersionUID = 1L;

        {
            put(Material.WHEAT, Material.WHEAT_SEEDS);
            put(Material.POTATOES, Material.POTATO);
            put(Material.CARROTS, Material.CARROT);
            put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
            put(Material.NETHER_WART, Material.NETHER_WART);
            put(Material.SWEET_BERRY_BUSH, Material.SWEET_BERRIES);
        }
    };

    private static final Map<Material, Material> TREES = new HashMap<>() {
        private static final long serialVersionUID = 1L;

        {
            put(Material.ACACIA_LOG, Material.ACACIA_SAPLING);
            put(Material.ACACIA_WOOD, Material.ACACIA_SAPLING);
            put(Material.SPRUCE_LOG, Material.SPRUCE_SAPLING);
            put(Material.SPRUCE_WOOD, Material.SPRUCE_SAPLING);
            put(Material.BIRCH_LOG, Material.BIRCH_SAPLING);
            put(Material.BIRCH_WOOD, Material.BIRCH_SAPLING);
            put(Material.JUNGLE_LOG, Material.JUNGLE_SAPLING);
            put(Material.JUNGLE_WOOD, Material.JUNGLE_SAPLING);
            put(Material.DARK_OAK_LOG, Material.DARK_OAK_SAPLING);
            put(Material.DARK_OAK_WOOD, Material.DARK_OAK_SAPLING);
            put(Material.OAK_LOG, Material.OAK_SAPLING);
            put(Material.OAK_WOOD, Material.OAK_SAPLING);
            put(Material.STRIPPED_ACACIA_LOG, Material.ACACIA_SAPLING);
            put(Material.STRIPPED_ACACIA_WOOD, Material.ACACIA_SAPLING);
            put(Material.STRIPPED_SPRUCE_LOG, Material.SPRUCE_SAPLING);
            put(Material.STRIPPED_SPRUCE_WOOD, Material.SPRUCE_SAPLING);
            put(Material.STRIPPED_BIRCH_LOG, Material.BIRCH_SAPLING);
            put(Material.STRIPPED_BIRCH_WOOD, Material.BIRCH_SAPLING);
            put(Material.STRIPPED_JUNGLE_LOG, Material.JUNGLE_SAPLING);
            put(Material.STRIPPED_JUNGLE_WOOD, Material.JUNGLE_SAPLING);
            put(Material.STRIPPED_DARK_OAK_LOG, Material.DARK_OAK_SAPLING);
            put(Material.STRIPPED_DARK_OAK_WOOD, Material.DARK_OAK_SAPLING);
            put(Material.STRIPPED_OAK_LOG, Material.OAK_SAPLING);
            put(Material.STRIPPED_OAK_WOOD, Material.OAK_SAPLING);
        }
    };

    public void start() {
        HandlerList.unregisterAll(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Replant)) {
            return false;
        }
        Replant replant = (Replant) o;
        return Objects.equals(plugin, replant.plugin) && Objects.equals(config, replant.config) && Objects.equals(playerData, replant.playerData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plugin, config, playerData);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!config.getAutoReplantWorlds().contains(block.getWorld().getName())) {
            return;
        }

        Material type = block.getType();

        if (isFarmLand(block) || isYoungPlant(block)) {
            event.setCancelled(true);
            return;
        }

        if (PLANTS.containsKey(type)) {
            replantSeed(event);
            return;
        }
        
        if (TREES.containsKey(type)) {
            replantSapling(block);
            return;
        }

        if (type == Material.CHORUS_FLOWER || type == Material.CHORUS_PLANT) {
            replantChorus(block);
            return;
        }
    }

    private void replantSeed(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        Material brokenBlockType = brokenBlock.getType();
        Player player = event.getPlayer();
        Material seed = PLANTS.get(brokenBlockType);

        if (!hasSeed(player, seed)) {
            event.setCancelled(true);
            return;
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                Block block = brokenBlock.getLocation().getBlock();
                if (!block.getType().equals(Material.AIR)) {
                    return;
                }

                block.setType(brokenBlockType);
                takeSeed(player, seed);
            }
        }.runTaskLater(plugin, 3L);
    }

    private void replantSapling(Block block) {
        Material sapling = TREES.get(block.getType());
        Material blockBelow = block.getRelative(BlockFace.DOWN).getBlockData().getMaterial();
        
        if (blockBelow != Material.DIRT && blockBelow != Material.GRASS_BLOCK && blockBelow != Material.PODZOL) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Location blockLoc = block.getLocation();
                blockLoc.getBlock().setType(sapling);
                blockLoc.getBlock().setBlockData(sapling.createBlockData());
            }
        }.runTaskLater(plugin, 3L);
    }

    private boolean isYoungPlant(Block block) {
        BlockData blockData = block.getBlockData();
        if (block.getType() == Material.SUGAR_CANE) {
            return block.getRelative(BlockFace.DOWN).getType() != Material.SUGAR_CANE;
        }

        if (block.getType() == Material.CHORUS_FLOWER) {
            return block.getRelative(BlockFace.DOWN).getType() == Material.END_STONE;
        }

        if (blockData instanceof Sapling) {
            Sapling sapling = (Sapling) blockData;
            return sapling.getStage() < sapling.getMaximumStage();
        }

        if (blockData instanceof Ageable) {
            Ageable ageable = (Ageable) blockData;
            return ageable.getAge() < ageable.getMaximumAge();
        }

        return false;
    }

    private boolean isFarmLand(Block block) {

        Material ground = block.getType();
        Material plant = block.getRelative(BlockFace.UP).getType();

        if (TREES.containsKey(plant)
                && (ground == Material.DIRT || ground == Material.GRASS_BLOCK || ground == Material.PODZOL)) {
            return true;
        }

        if (PLANTS.containsKey(plant) && ground == Material.FARMLAND) {
            return true;
        }

        if ((plant == Material.CHORUS_FLOWER || plant == Material.CHORUS_PLANT) && ground == Material.END_STONE) {
            return true;
        }

        // 作物がネザーウォートなら下は必ずソウルサンドなのでチェック不要
        if (plant == Material.NETHER_WART) {
            return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void cancelBoneMeal(PlayerInteractEvent event) {
        if (!config.getAutoReplantWorlds().contains(event.getPlayer().getWorld().getName())) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (!TREES.containsValue(clickedBlock.getType())) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.BONE_MEAL
                && event.getPlayer().getInventory().getItemInOffHand().getType() != Material.BONE_MEAL) {
            return;
        }
        event.setCancelled(true);
    }

    private void replantChorus(Block chorus) {
        if (chorus.getType() != Material.CHORUS_PLANT && chorus.getType() != Material.CHORUS_FLOWER) {
            return;
        }

        
        new BukkitRunnable(){
            
            @Override
            public void run() {
                chorus.breakNaturally();
                if (chorus.getRelative(BlockFace.DOWN).getType() == Material.END_STONE) {
                    chorus.setType(Material.CHORUS_FLOWER);
                    return;
                }
                replantChorus(chorus.getRelative(BlockFace.NORTH));
                replantChorus(chorus.getRelative(BlockFace.EAST));
                replantChorus(chorus.getRelative(BlockFace.WEST));
                replantChorus(chorus.getRelative(BlockFace.SOUTH));
                replantChorus(chorus.getRelative(BlockFace.DOWN));
            }
        }.runTaskLater(plugin, 1L);
    }

    private void takeSeed(Player player, Material seed) {
        if (!PLANTS.containsValue(seed)) {
            return;
        }

        ItemStack seedItem = new ItemStack(seed);
        int stock = playerData.getStock(player, seedItem);

        if (stock >= 1) {
            playerData.setStock(player, seedItem, stock - 1);
        } else {
            player.getInventory().removeItem(new ItemStack(seed));
        }
    }

    private boolean hasSeed(Player player, Material seed) {
        if (!PLANTS.containsValue(seed)) {
            return false;
        }

        if (player.getInventory().contains(seed)) {
            return true;
        }

        return playerData.getStock(player, new ItemStack(seed)) > 0;
    }
}