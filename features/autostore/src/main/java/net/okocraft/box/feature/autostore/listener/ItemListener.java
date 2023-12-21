package net.okocraft.box.feature.autostore.listener;

import io.papermc.paper.event.block.PlayerShearBlockEvent;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.feature.autostore.integration.CoreProtectIntegration;
import net.okocraft.box.feature.autostore.model.AutoStoreSettingContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemListener implements Listener {

    private static final StockEvent.Cause AUTOSTORE_CAUSE = StockEvent.Cause.create("autostore");

    public void register() {
        var plugin = BoxAPI.api().getPluginInstance();
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

        if (processEvent(player, event.getItem().getLocation(), event.getItem().getItemStack(), false)) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItem(@NotNull BlockDropItemEvent event) {
        var location = event.getBlock().getLocation();
        event.getItems().removeIf(item -> processEvent(event.getPlayer(), location, item.getItemStack(), true));
    }

    /**
     * For killing entities.
     *
     * @param event fired on players harvest berries.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        var killed = event.getEntity();
        if (killed instanceof Mob && !(killed instanceof Boss) && killed.getKiller() != null) {
            var location = killed.getLocation();
            event.getDrops().removeIf(item -> processEvent(killed.getKiller(), location, item, true));
        }
    }

    /**
     * For glow berries and sweet berries.
     *
     * @param event fired on players harvest berries.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHarvestBlock(@NotNull PlayerHarvestBlockEvent event) {
        var location = event.getHarvestedBlock().getLocation();
        event.getItemsHarvested().removeIf(item -> processEvent(event.getPlayer(), location, item, true));
    }

    /**
     * For pumpkin seed and honeycomb.
     *
     * @param event fired on players shear pumpkins or honeycombs.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShearBlock(@NotNull PlayerShearBlockEvent event) {
        var location = event.getBlock().getLocation();
        event.getDrops().removeIf(item -> processEvent(event.getPlayer(), location, item, true));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowPickup(@NotNull PlayerPickupArrowEvent event) {
        if (processEvent(event.getPlayer(), event.getArrow().getLocation(), event.getArrow().getItemStack(), false)) {
            event.setCancelled(true);
            event.getArrow().remove();
        }
    }

    private boolean processEvent(@NotNull Player player, @NotNull Location location, @NotNull ItemStack item, boolean direct) {
        if (!BoxAPI.api().canUseBox(player)) {
            return false;
        }

        var playerMap = BoxAPI.api().getBoxPlayerMap();
        var container = AutoStoreSettingContainer.INSTANCE;

        if (!playerMap.isLoaded(player) || !container.isLoaded(player)) {
            return false;
        }

        var setting = container.get(player);

        if (!setting.isEnabled() || !player.hasPermission("box.autostore")) {
            return false;
        }

        if (direct && !setting.isDirect()) {
            return false;
        }

        var boxItem = BoxAPI.api().getItemManager().getBoxItem(item);

        if (boxItem.isEmpty()) {
            return false;
        }

        if (setting.shouldAutoStore(boxItem.get())) {
            playerMap.get(player).getCurrentStockHolder().increase(boxItem.get(), item.getAmount(), AUTOSTORE_CAUSE);
            if (!direct) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, (float) Math.random() + 1.0f);
            }

            CoreProtectIntegration.logItemPickup(player, location, item);

            return true;
        } else {
            return false;
        }
    }
}
