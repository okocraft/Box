package net.okocraft.box.plugin.listener.stick;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.BoxPermission;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ItemConsumeListener extends AbstractStickListener {

    public ItemConsumeListener(@NotNull Box plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemConsume(@NotNull PlayerItemConsumeEvent e) {
        if (isInDisabledWorld(e.getPlayer())
                || isCreative(e.getPlayer())
                || !hasStick(e.getPlayer(), false)
                || !BoxPermission.BOX_STICK_CONSUME.has(e.getPlayer())
        ) {
            return;
        }

        // TODO: 権限チェック

        Optional<Item> item = plugin.getItemManager().getItem(e.getItem());

        if (item.isEmpty()) {
            return;
        }

        User user = plugin.getUserManager().getUser(e.getPlayer().getUniqueId());

        if (user.hasStock(item.get())) {
            plugin.getDataHandler().decrease(user, item.get());
            e.setItem(item.get().getOriginalCopy());
        }

        // TODO: 残りの所持数表示
    }
}
