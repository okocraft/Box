/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.okocraft.box.worldedit;

import java.util.HashMap;
import java.util.Map;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.okocraft.box.Box;
import net.okocraft.box.database.PlayerData;

/**
 * Applies a {@link BlockBag} to operations.
 */
public class ConsumeBlockFromBoxExtent extends AbstractDelegateExtent {

    private final Player player;
    private Map<BlockType, Integer> missingBlocks = new HashMap<>();

    /**
     * Create a new instance.
     *
     * @param extent   the extent
     * @param blockBag the block bag
     */
    public ConsumeBlockFromBoxExtent(Extent extent, Player player) {
        super(extent);
        this.player = player;
    }

    /**
     * Gets the list of missing blocks and clears the list for the next operation.
     *
     * @return a map of missing blocks
     */
    public Map<BlockType, Integer> popMissing() {
        Map<BlockType, Integer> missingBlocks = this.missingBlocks;
        this.missingBlocks = new HashMap<>();
        return missingBlocks;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block) throws WorldEditException {
        if (!player.hasPermission("box.worldedit.item.unrestricted")) {

            if (WorldEdit.getInstance().getConfiguration().useInventory) {
                return super.setBlock(position, block);
            }

            BlockState existing = getExtent().getBlock(position);

            if (block.getBlockType().equals(existing.getBlockType())) {
                return super.setBlock(position, block);
            }

            if (!block.getBlockType().getMaterial().isAir()) {
                Material material = Material.matchMaterial(block.getBlockType().getId());
                if (!Box.getInstance().getGeneralConfig().getAllItems().contains(material.name())) {
                    return false;
                }
                ItemStack item = new ItemStack(material);
                if (PlayerData.getItemAmount(player, item) > 0) {
                    PlayerData.addItemAmount(player, item, -1);
                } else {
                    if (!missingBlocks.containsKey(block.getBlockType())) {
                        missingBlocks.put(block.getBlockType(), 1);
                    } else {
                        missingBlocks.put(block.getBlockType(), missingBlocks.get(block.getBlockType()) + 1);
                    }
                    return false;
                }
            }

            if (!existing.getBlockType().getMaterial().isAir()) {
                Material material = Material.matchMaterial(existing.getBlockType().getId());
                if (!Box.getInstance().getGeneralConfig().getAllItems().contains(material.name())) {
                    return false;
                }
                ItemStack item = new ItemStack(material);
                PlayerData.addItemAmount(player, item, 1);
            }
        }

        return super.setBlock(position, block);
    }
}
