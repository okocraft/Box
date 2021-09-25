package net.okocraft.box.feature.autostore.listener;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.model.AutoStoreSettingContainer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemListener implements Listener {

    public void register() {
        var plugin = BoxProvider.get().getPluginInstance();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(@NotNull EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (processEvent(player, event.getItem().getItemStack())) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowPickup(@NotNull PlayerPickupArrowEvent event) {
        if (processEvent(event.getPlayer(), event.getArrow().getItemStack())) {
            event.setCancelled(true);
            event.getArrow().remove();
        }
    }

    private boolean processEvent(@NotNull Player player, @NotNull ItemStack item) {
        if (BoxProvider.get().isDisabledWorld(player)) {
            return false;
        }

        if (!player.hasPermission("box.autostore")) {
            return false;
        }

        var boxItem = BoxProvider.get().getItemManager().getBoxItem(item);

        if (boxItem.isEmpty()) {
            return false;
        }

        var setting = AutoStoreSettingContainer.INSTANCE.get(player);

        if (setting.isEnabled() && setting.shouldAutoStore(boxItem.get())) {
            BoxProvider.get()
                    .getBoxPlayerMap()
                    .get(player)
                    .getCurrentStockHolder()
                    .increase(boxItem.get(), item.getAmount());

            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, (float) Math.random() + 1.0f);
            return true;
        } else {
            return false;
        }
    }
}
