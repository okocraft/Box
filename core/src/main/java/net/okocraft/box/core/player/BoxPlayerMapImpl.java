package net.okocraft.box.core.player;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.util.Debugger;
import net.okocraft.box.core.message.ErrorMessages;
import net.okocraft.box.core.model.user.BoxUserImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class BoxPlayerMapImpl implements BoxPlayerMap {

    private final Map<Player, BoxPlayer> playerMap = new ConcurrentHashMap<>();
    private final UserManager userManager;
    private final StockManager stockManager;

    public BoxPlayerMapImpl(@NotNull UserManager userManager, @NotNull StockManager stockManager) {
        this.userManager = userManager;
        this.stockManager = stockManager;
    }

    @Override
    public @NotNull BoxPlayer get(@NotNull Player player) {
        return Optional.ofNullable(playerMap.get(player))
                .orElseThrow(() -> new IllegalStateException("player is not loaded (" + player.getName() + ")"));
    }

    public @NotNull CompletableFuture<Void> load(@NotNull Player player) {
        return CompletableFuture.runAsync(() -> {
            var boxUser = new BoxUserImpl(player.getUniqueId(), player.getName());

            updateUserName(boxUser);

            Debugger.log(() -> "Loading player stock data... (" + player.getName() + ")");

            var userStock = stockManager.loadUserStock(boxUser).join();

            playerMap.put(player, new BoxPlayerImpl(player, userStock));
        });
    }

    public @NotNull CompletableFuture<Void> unload(@NotNull Player player) {
        var boxPlayer = playerMap.remove(player);

        if (boxPlayer != null) {
            Debugger.log(() -> "Unloading player data... (" + player.getName() + ")");
            return stockManager.saveUserStock(boxPlayer.getUserStockHolder());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    public void loadAll() {
        playerMap.clear();

        for (var player : Bukkit.getOnlinePlayers()) {
            load(player).exceptionallyAsync(e -> {
                BoxProvider.get().getLogger().log(Level.SEVERE, "Could not load a player (" + player.getName()+ ")", e);
                player.sendMessage(ErrorMessages.ERROR_LOAD_PLAYER_DATA_ON_JOIN);
                return null;
            });
        }
    }

    public void unloadAll() {
        for (var boxPlayer : playerMap.values()) {
            Debugger.log(() -> "Unloading player data... (" + boxPlayer.getName().orElse("Unknown") + ")");
            stockManager.saveUserStock(boxPlayer.getUserStockHolder())
                    .exceptionallyAsync(e -> {
                        BoxProvider.get().getLogger().log(Level.SEVERE,
                                "Could not save player data (" + boxPlayer.getName() + ")", e);
                        return null;
                    });
        }

        playerMap.clear();
    }

    private void updateUserName(@NotNull BoxUser user) {
        Debugger.log(() -> "Updating player's uuid and name... (" + user.getName().orElse("Unknown") + ")");

        userManager.saveUser(user)
                .exceptionallyAsync(e -> {
                    BoxProvider.get().getLogger().log(Level.SEVERE,
                            "Could not save the uuid and name of player (" + user.getName() + ")", e);
                    return null;
                });
    }
}
