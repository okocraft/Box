package net.okocraft.box.core.listener;

import net.okocraft.box.core.player.BoxPlayerMapImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerConnectionListener implements Listener {

    private final BoxPlayerMapImpl playerMap;

    public PlayerConnectionListener(@NotNull BoxPlayerMapImpl playerMap) {
        this.playerMap = playerMap;
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        playerMap.scheduleLoadingData(event.getPlayer());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        playerMap.unload(event.getPlayer());
    }
}
