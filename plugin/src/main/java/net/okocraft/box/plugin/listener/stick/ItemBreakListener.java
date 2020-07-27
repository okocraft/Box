package net.okocraft.box.plugin.listener.stick;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ItemBreakListener extends AbstractStickListener {

    public ItemBreakListener(@NotNull Box plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemBreak(@NotNull PlayerItemBreakEvent e) {
        if (isInDisabledWorld(e.getPlayer())
                || !hasStick(e.getPlayer(), false)
                || isCreative(e.getPlayer())) {
            return;
        }

        // TODO: 権限チェック

        ItemStack broken = e.getBrokenItem().clone();
        ItemMeta meta = broken.getItemMeta();

        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(0);
        }

        broken.setItemMeta(meta);

        Optional<Item> item = plugin.getItemManager().getItem(broken);

        if (item.isEmpty()) {
            return;
        }

        User user = plugin.getUserManager().getUser(e.getPlayer().getUniqueId());

        if (user.hasStock(item.get())) {
            user.decrease(item.get());
            e.getBrokenItem().setAmount(e.getBrokenItem().getAmount() + 1);
        }

        // TODO: 残りの所持数表示
    }
}
