package net.okocraft.box.plugin.listener;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.sound.BoxSound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ItemPickupListener extends AbstractListener {

    public ItemPickupListener(@NotNull Box plugin) {
        super(plugin);
    }

    @EventHandler
    public void onItemPickup(@NotNull EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getEntity();

        if (plugin.getGeneralConfig().getDisabledWorlds().contains(player.getWorld().getName())) {
            return;
        }

        Optional<Item> item = plugin.getItemManager().getItem(e.getItem().getItemStack());

        if (item.isEmpty()) {
            return;
        }

        User user = plugin.getUserManager().getUser(player.getUniqueId());

        if (user.isAutoStore(item.get())) {
            user.increase(item.get());
            e.setCancelled(true);
            plugin.getSoundPlayer().play(player, BoxSound.ITEM_DEPOSIT);

            // TODO アクションバーにアイテム所持数表示 (要検討)
        }
    }
}
