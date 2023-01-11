package net.okocraft.box.feature.stick.function.menu;

import com.github.siroshun09.configapi.api.value.ConfigValue;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public final class MenuOpener {

    private static final ConfigValue<String> MENU_COMMAND_SETTING = config -> config.getString("stick.menu-command", "box gui");

    public static void openMenu(@NotNull PlayerInteractEvent event, @NotNull BoxStickItem boxStickItem) {
        var player = event.getPlayer();

        boolean shouldOpen;

        if (event.getHand() == EquipmentSlot.HAND) {
            shouldOpen = boxStickItem.check(player.getInventory().getItemInMainHand());
        } else { // OFF_HAND
            shouldOpen = player.getInventory().getItemInMainHand().getType().isAir() && boxStickItem.check(player.getInventory().getItemInOffHand());
        }

        if (shouldOpen && player.hasPermission("box.stick.menu")) {
            var command = BoxProvider.get().getConfiguration().get(MENU_COMMAND_SETTING);

            if (!command.isEmpty()) {
                Bukkit.dispatchCommand(player, command);
            }
        }
    }
}
