package net.okocraft.box.plugin.listener.stick;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.BoxPermission;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BlockPlaceListener extends AbstractStickListener {

    public BlockPlaceListener(@NotNull Box plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(@NotNull BlockPlaceEvent e) {
        if (isInDisabledWorld(e.getPlayer())
                || isCreative(e.getPlayer())
                || !hasStick(e.getPlayer(), false)
                || !BoxPermission.BOX_STICK_PLACE.has(e.getPlayer())
        ) {
            return;
        }

        Optional<Item> item = plugin.getItemManager().getItem(e.getItemInHand());

        if (item.isEmpty()) {
            return;
        }

        User user = plugin.getUserManager().getUser(e.getPlayer().getUniqueId());

        if (user.hasStock(item.get())) {
            plugin.getDataHandler().decrease(user, item.get());
            e.getPlayer().getInventory().setItemInMainHand(e.getItemInHand());
        }

        // TODO: 残りの所持数表示
    }
}
