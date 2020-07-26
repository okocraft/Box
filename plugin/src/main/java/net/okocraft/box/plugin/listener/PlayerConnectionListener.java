package net.okocraft.box.plugin.listener;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.manager.UserManager;
import net.okocraft.box.plugin.result.UserCheckResult;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerConnectionListener extends AbstractListener {

    public PlayerConnectionListener(@NotNull Box plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(@NotNull AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        UserCheckResult checkResult = plugin.getUserManager().checkUser(e.getUniqueId(), e.getName());
        plugin.debug("User " + e.getName() + " (" + e.getUniqueId().toString() + ") check result: " + checkResult);
    }


    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        plugin.getExecutor().submit(() -> plugin.getUserManager().loadUser(e.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        plugin.getExecutor().submit(() -> {
            UserManager userManager = plugin.getUserManager();
            UUID uuid = e.getPlayer().getUniqueId();

            if (userManager.isLoaded(uuid)) {
                User user = userManager.getUser(e.getPlayer().getUniqueId());
                userManager.saveUser(user);
                userManager.unloadUser(user);
            }
        });
    }
}
