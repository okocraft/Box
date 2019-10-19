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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.okocraft.box.Box;
import net.okocraft.box.config.Config;
import net.okocraft.box.database.PlayerData;
import org.jetbrains.annotations.Nullable;

public class Replant implements Listener {
    @Nullable
    private static final Box plugin = Box.getInstance();

    private static final Map<Material, Material> PLANTS = new HashMap<>() {
        private static final long serialVersionUID = 1L;

        {
            put(Material.WHEAT, Material.WHEAT_SEEDS);
            put(Material.POTATOES, Material.POTATO);
            put(Material.CARROTS, Material.CARROT);
            put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
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

    public Replant() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void replantSeed(BlockBreakEvent event) {

        if (event.isCancelled())
            return;

        Block brokenBlock = event.getBlock();

        if (!Config.getConfig().getAutoReplantWorlds().contains(brokenBlock.getWorld())) {
            return;
        }

        Material brokenBlockType = brokenBlock.getType();
        Player player = event.getPlayer();

        if (!PLANTS.containsKey(brokenBlockType)) {
            return;
        }

        Material seed = PLANTS.get(brokenBlockType);

        if (!hasSeed(player, seed)) {
            event.setCancelled(true);
            return;
        }

        Ageable blockDataAgable = (Ageable) brokenBlock.getBlockData();
        if (blockDataAgable.getAge() != blockDataAgable.getMaximumAge()) {
            event.setCancelled(true);
            return;
        }

        Ageable newBlockDataAgeable = (Ageable) blockDataAgable.clone();
        newBlockDataAgeable.setAge(0);

        new BukkitRunnable() {

            @Override
            public void run() {
                Block block = brokenBlock.getLocation().getBlock();
                if (!block.getType().equals(Material.AIR))
                    return;
                block.setType(brokenBlockType);
                block.setBlockData(newBlockDataAgeable);

                takeSeed(player, seed);
            }
        }.runTaskLater(plugin, 3L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void replantSapling(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block treeBlock = event.getBlock();

        if (!Config.getConfig().getAutoReplantWorlds().contains(treeBlock.getWorld())) {
            return;
        }

        Material treeMaterial = treeBlock.getType();

        if (TREES.containsValue(treeMaterial)) {
            event.setCancelled(true);
            return;
        }

        if (!TREES.containsKey(treeMaterial)) {
            return;
        }

        Material sapling = TREES.get(treeMaterial);

        Material blockBelow = treeBlock.getLocation().add(0D, -1D, 0D).getBlock().getBlockData().getMaterial();
        if (!blockBelow.equals(Material.DIRT) && !blockBelow.equals(Material.GRASS_BLOCK)
                && !blockBelow.equals(Material.PODZOL)) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Location blockLoc = treeBlock.getLocation();
                blockLoc.getBlock().setType(sapling);
                blockLoc.getBlock().setBlockData(sapling.createBlockData());
            }
        }.runTaskLater(plugin, 3L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void cancelBreakingDirt(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Material ground = event.getBlock().getType();
        if (!ground.equals(Material.DIRT) && !ground.equals(Material.GRASS_BLOCK)
                && !ground.equals(Material.PODZOL)) {
            return;
        }

        Material sapling = event.getBlock().getLocation().add(0, 1, 0).getBlock().getType();
        if (TREES.containsValue(sapling)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void cancelBoneMeal(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (!TREES.containsValue(clickedBlock.getType())) {
            return;
        }
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.BONE_MEAL)
                && !event.getPlayer().getInventory().getItemInOffHand().getType().equals(Material.BONE_MEAL)) {
            return;
        }
        event.setCancelled(true);
    }

    private void takeSeed(Player player, Material seed) {
        if (!PLANTS.containsValue(seed)) {
            return;
        }

        ItemStack seedItem = new ItemStack(seed);
        long stock = PlayerData.getItemAmount(player, seedItem);

        if (stock >= 1) {
            PlayerData.setItemAmount(player, seedItem, stock - 1);
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

        return PlayerData.getItemAmount(player, new ItemStack(seed)) > 0;
    }
}