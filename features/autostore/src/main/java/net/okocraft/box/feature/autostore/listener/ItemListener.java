package net.okocraft.box.feature.autostore.listener;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.model.SettingManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class ItemListener implements Listener {

    private final SettingManager settingManager;

    public ItemListener(@NotNull SettingManager settingManager) {
        this.settingManager = settingManager;
    }

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

        if (BoxProvider.get().isDisabledWorld(player)) {
            return;
        }

        if (!player.hasPermission("box.autostore")) {
            return;
        }

        var item = event.getItem().getItemStack();

        var boxItem = BoxProvider.get().getItemManager().getBoxItem(item);

        if (boxItem.isEmpty()) {
            return;
        }

        var setting = settingManager.get(player);

        if (setting.getCurrentMode().isEnabled(boxItem.get())) {
            BoxProvider.get()
                    .getBoxPlayerMap()
                    .get(player)
                    .getCurrentStockHolder()
                    .increase(boxItem.get(), item.getAmount());

            event.getItem().remove();
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, (float) Math.random() + 1.0f);
        }
    }
}
