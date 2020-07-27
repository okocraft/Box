package net.okocraft.box.plugin.listener;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class StickListener extends AbstractListener {

    public StickListener(@NotNull Box plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(@NotNull PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND || isInDisabledWorld(e.getPlayer())) {
            return;
        }

        // TODO: 権限チェック?

        if (hasStick(e.getPlayer(), true)) {
            // TODO: GUI を開く
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(@NotNull BlockPlaceEvent e) {
        if (isInDisabledWorld(e.getPlayer())
                || !hasStick(e.getPlayer(), false)
                || isCreative(e.getPlayer())) {
            return;
        }

        // TODO: 権限チェック

        Optional<Item> item = plugin.getItemManager().getItem(e.getItemInHand());

        if (item.isEmpty()) {
            return;
        }

        User user = plugin.getUserManager().getUser(e.getPlayer().getUniqueId());

        if (user.hasStock(item.get())) {
            user.decrease(item.get());
            // TODO: インベントリのアイテム
        }

        // TODO: 残りの所持数表示
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemConsume(@NotNull PlayerItemConsumeEvent e) {
        if (isInDisabledWorld(e.getPlayer())
                || !hasStick(e.getPlayer(), false)
                || isCreative(e.getPlayer())) {
            return;
        }

        // TODO: 権限チェック

        Optional<Item> item = plugin.getItemManager().getItem(e.getItem());

        if (item.isEmpty()) {
            return;
        }

        User user = plugin.getUserManager().getUser(e.getPlayer().getUniqueId());

        if (user.hasStock(item.get())) {
            user.decrease(item.get());
            e.setItem(item.get().getOriginalCopy());
        }

        // TODO: 残りの所持数表示
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

    private boolean isInDisabledWorld(@NotNull Player player) {
        return plugin.getGeneralConfig().getDisabledWorlds().contains(player.getWorld().getName());
    }

    private boolean hasStick(@NotNull Player player, boolean isMain) {
        if (isMain) {
            return plugin.getItemManager().getStick().isStick(player.getInventory().getItemInMainHand());
        } else {
            return plugin.getItemManager().getStick().isStick(player.getInventory().getItemInOffHand());
        }
    }

    private boolean isCreative(@NotNull Player player) {
        return player.getGameMode() == GameMode.CREATIVE;
    }
}
