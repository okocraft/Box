package net.okocraft.box.plugin.listener.stick;

import net.okocraft.box.plugin.Box;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class InteractListener extends AbstractStickListener {

    public InteractListener(@NotNull Box plugin) {
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
}
