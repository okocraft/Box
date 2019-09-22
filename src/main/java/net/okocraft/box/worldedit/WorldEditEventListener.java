package net.okocraft.box.worldedit;

import java.util.Map;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.EditSession.Stage;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockType;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.okocraft.box.Box;

public class WorldEditEventListener {

    private static final WorldEdit worldEdit = WorldEdit.getInstance();
    private static WorldEditEventListener listener;

    private WorldEditEventListener() {
    }

    public static void register() {
        if (listener == null) {
            listener = new WorldEditEventListener();
        }
        worldEdit.getEventBus().register(listener);
    }
    
    public static void unregister() {
        if (listener != null) {
            worldEdit.getEventBus().unregister(listener);
            listener = null;
        }
    }

    @Subscribe
    public void onEditSessionEvent(EditSessionEvent event) {
        if (event.getStage() != Stage.BEFORE_CHANGE) {
            return;
        }
        Player player = Bukkit.getPlayer(event.getActor().getUniqueId());
        if (player == null) {
            return;
        }
        Extent extent = event.getExtent();
        ConsumeBlockFromBoxExtent consume;
        extent = new PreventReplacingDisallowExtent(extent, player);    
        extent = consume = new ConsumeBlockFromBoxExtent(extent, player);
        event.setExtent(extent);
        new BukkitRunnable(){
            @Override
            public void run() {
                Map<BlockType, Integer> missing = consume.popMissing();
                if (missing.isEmpty()) {
                    return;
                }
                player.sendMessage("§8[§6Box§8] §7不足したブロック:");
                missing.forEach((type, amount) -> player.sendMessage("  §b" + type.getName() + "§7: §b" + amount));
            }
        }.runTaskLater(Box.getInstance(), 3L);
    }
}