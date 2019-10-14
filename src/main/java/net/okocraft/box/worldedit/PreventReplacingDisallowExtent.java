package net.okocraft.box.worldedit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import org.bukkit.entity.Player;

public class PreventReplacingDisallowExtent extends AbstractDelegateExtent {

    private final Player player;

    public PreventReplacingDisallowExtent(Extent extent, Player player) {
        super(extent);
        this.player = player;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
        if (!player.hasPermission("box.worldedit.disallowblock.unrestricted")) {
            BlockState existing = getExtent().getBlock(location);
            if (WorldEdit.getInstance().getConfiguration().disallowedBlocks.contains(existing.getBlockType().getId())) {
                return false;
            }
            if (WorldEdit.getInstance().getConfiguration().disallowedBlocks.contains(block.getBlockType().getId())) {
                return false;
            }
        }

        return super.setBlock(location, block);
    }
}