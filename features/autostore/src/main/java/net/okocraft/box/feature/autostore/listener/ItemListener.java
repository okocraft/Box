package net.okocraft.box.feature.autostore.listener;

import io.papermc.paper.event.block.PlayerShearBlockEvent;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.stockholder.stock.StockActor;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.feature.autostore.AutoStoreSettingProvider;
import net.okocraft.box.feature.autostore.event.AutoStoreItemEvent;
import net.okocraft.box.feature.autostore.integration.CoreProtectIntegration;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Boss;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class ItemListener implements Listener {

    private static final StockEvent.Cause AUTOSTORE_CAUSE = StockEvent.Cause.create("autostore");
    private final AutoStoreSettingProvider container;

    public ItemListener(@NotNull AutoStoreSettingProvider container) {
        this.container = container;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(@NotNull EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        var stored = this.storeItem(player, event.getItem().getLocation(), event.getItem().getItemStack(), event.getItem().getUniqueId(), false);
        if (stored != null) {
            event.setCancelled(true);
            event.getItem().remove();
            notifyStored(stored);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItem(@NotNull BlockDropItemEvent event) {
        Location location = event.getBlock().getLocation();
        storeDrops(event.getItems(), item -> this.storeItem(event.getPlayer(), location, item.getItemStack(), item.getUniqueId(), true));
    }

    /**
     * For killing entities.
     *
     * @param event fired on players harvest berries.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        LivingEntity killed = event.getEntity();
        if (killed instanceof Mob && !(killed instanceof Boss) && killed.getKiller() != null) {
            Location location = killed.getLocation();
            storeDrops(event.getDrops(), item -> this.storeItem(killed.getKiller(), location, item, null, true));
        }
    }

    /**
     * For glow berries and sweet berries.
     *
     * @param event fired on players harvest berries.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHarvestBlock(@NotNull PlayerHarvestBlockEvent event) {
        Location location = event.getHarvestedBlock().getLocation();
        storeDrops(event.getItemsHarvested(), item -> this.storeItem(event.getPlayer(), location, item, null, true));
    }

    /**
     * For pumpkin seed and honeycomb.
     *
     * @param event fired on players shear pumpkins or honeycombs.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShearBlock(@NotNull PlayerShearBlockEvent event) {
        Location location = event.getBlock().getLocation();
        storeDrops(event.getDrops(), item -> this.storeItem(event.getPlayer(), location, item, null, true));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowPickup(@NotNull PlayerPickupArrowEvent event) {
        var stored = this.storeItem(event.getPlayer(), event.getArrow().getLocation(), event.getArrow().getItemStack(), event.getArrow().getUniqueId(), false);
        if (stored != null) {
            event.setCancelled(true);
            event.getArrow().remove();
            notifyStored(stored);
        }
    }

    private static <T> void storeDrops(List<T> drops, Function<T, @Nullable AutoStoreItemEvent> store) {
        var stored = new ArrayList<AutoStoreItemEvent>();
        drops.removeIf(drop -> {
            var event = store.apply(drop);
            if (event == null) {
                return false;
            }
            stored.add(event);
            return true;
        });
        stored.forEach(ItemListener::notifyStored);
    }

    private static void notifyStored(AutoStoreItemEvent event) {
        BoxAPI.api().getEventCallers().sync().call(event);
    }

    private @Nullable AutoStoreItemEvent storeItem(@NotNull Player player, @NotNull Location location, @NotNull ItemStack item,
                                                  @Nullable UUID sourceEntityId, boolean direct) {
        if (!BoxAPI.api().canUseBox(player)) {
            return null;
        }

        BoxPlayerMap playerMap = BoxAPI.api().getBoxPlayerMap();
        AutoStoreSetting setting = this.container.getIfLoaded(player.getUniqueId());

        if (!playerMap.isLoaded(player) || setting == null ||
            !setting.isEnabled() || !player.hasPermission("box.autostore") ||
            (direct && !setting.isDirect())
        ) {
            return null;
        }

        Optional<BoxItem> boxItem = BoxAPI.api().getItemManager().getBoxItem(item);

        if (boxItem.isEmpty() || !setting.shouldAutoStore(boxItem.get())) {
            return null;
        }

        playerMap.get(player).getCurrentStockHolder().increase(boxItem.get(), item.getAmount(), AUTOSTORE_CAUSE);
        if (!direct) {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, (float) Math.random() + 1.0f);
        }

        CoreProtectIntegration.logItemPickup(player, location, item);

        return new AutoStoreItemEvent(StockActor.fromPlayer(player), location, item, sourceEntityId);
    }
}
