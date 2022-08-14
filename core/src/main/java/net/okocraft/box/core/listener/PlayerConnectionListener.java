package net.okocraft.box.core.listener;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.core.message.ErrorMessages;
import net.okocraft.box.core.player.BoxPlayerMapImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class PlayerConnectionListener implements Listener {

    private final BoxPlayerMapImpl playerMap;

    public PlayerConnectionListener(@NotNull BoxPlayerMapImpl playerMap) {
        this.playerMap = playerMap;
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        var player = event.getPlayer();

        playerMap.load(player)
                .exceptionallyAsync(throwable -> {
                    BoxProvider.get().getLogger().log(
                            Level.SEVERE,
                            "Could not load a player (" + player.getName() + ")",
                            throwable
                    );
                    player.sendMessage(ErrorMessages.ERROR_LOAD_PLAYER_DATA_ON_JOIN);
                    return null;
                });
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();

        playerMap.unload(player)
                .exceptionallyAsync(throwable -> {
                    BoxProvider.get().getLogger().log(
                            Level.SEVERE,
                            "Could not unload a player (" + player.getName() + ")",
                            throwable
                    );
                    return null;
                });
    }
}
