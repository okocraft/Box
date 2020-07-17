package net.okocraft.box.command.boxadmin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.okocraft.box.database.PlayerData;

class MigrateCommand extends BaseAdminCommand {

    MigrateCommand() {
        super("migrate", "boxadmin.migrate", 1, false, "/boxadmin migrate", new String[0]);
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        @SuppressWarnings("deprecation")
        PlayerData sqlPlayerData = new PlayerData();
                
        new BukkitRunnable() {

            @Override
            public void run() {

                Set<ItemStack> items = itemData.getNames().stream().map(itemData::getItemStack).collect(Collectors.toSet());
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    if (player.getName() == null) {
                        continue;
                    }

                    plugin.getLogger().info("migrating " + player.getName() + "'s data.");

                    Map<ItemStack, Integer> oldStock = new HashMap<>(sqlPlayerData.getStockAll(player));
                    Map<ItemStack, Boolean> oldAutostore = new HashMap<>(sqlPlayerData.getAutoStoreAll(player));

                    Set<ItemStack> removeItems = new HashSet<>();
                    items.forEach(item -> {
                        long stock = oldStock.getOrDefault(item, 0);
                        boolean autostore = oldAutostore.getOrDefault(item, false);

                        if (stock == 0 && autostore == false) {
                            removeItems.add(item);
                        }
                    });

                    oldStock.keySet().removeAll(removeItems);
                    oldAutostore.keySet().removeAll(removeItems);

                    playerData.setStockAll(player, oldStock);
                    playerData.setAutoStoreAll(player, oldAutostore);
                }

                sqlPlayerData.dispose();
            }
        }.runTaskAsynchronously(plugin);
        
        return true;
    }

}