package net.okocraft.box.core.player;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.player.PlayerLoadEvent;
import net.okocraft.box.api.event.player.PlayerUnloadEvent;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.core.message.ErrorMessages;
import net.okocraft.box.core.model.loader.UserStockHolderLoader;
import net.okocraft.box.core.model.user.BoxUserImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
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
    public boolean isLoaded(@NotNull Player player) {
        return playerMap.containsKey(player);
    }

    @Override
    public @NotNull BoxPlayer get(@NotNull Player player) {
        Objects.requireNonNull(player);

        return Optional.ofNullable(playerMap.get(player))
                .orElseThrow(() -> new IllegalStateException("player is not loaded (" + player.getName() + ")"));
    }

    public @NotNull CompletableFuture<Void> load(@NotNull Player player) {
        Objects.requireNonNull(player);

        return CompletableFuture.runAsync(() -> {
            var boxUser = new BoxUserImpl(player.getUniqueId(), player.getName());

            updateUserName(boxUser);

            var userStock = stockManager.loadUserStock(boxUser).join();

            var boxPlayer = new BoxPlayerImpl(player, userStock);

            playerMap.put(player, boxPlayer);

            BoxProvider.get().getEventBus().callEvent(new PlayerLoadEvent(boxPlayer));
        });
    }

    public @NotNull CompletableFuture<Void> unload(@NotNull Player player) {
        Objects.requireNonNull(player);

        var boxPlayer = playerMap.remove(player);

        if (boxPlayer != null) {
            return unloadAndSave(boxPlayer);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    public void loadAll() {
        playerMap.clear();

        for (var player : Bukkit.getOnlinePlayers()) {
            load(player).exceptionallyAsync(e -> {
                BoxProvider.get().getLogger().log(Level.SEVERE, "Could not load a player (" + player.getName() + ")", e);
                player.sendMessage(ErrorMessages.ERROR_LOAD_PLAYER_DATA_ON_JOIN);
                return null;
            });
        }
    }

    public void unloadAll() {
        for (var boxPlayer : playerMap.values()) {
            unloadAndSave(boxPlayer).exceptionally(e -> {
                if (boxPlayer.getPlayer().isOnline()) {
                    boxPlayer.getPlayer().sendMessage(ErrorMessages.ERROR_SAVE_PLAYER_DATA);
                }

                BoxProvider.get().getLogger().log(Level.SEVERE,
                        "Could not save player data (" + boxPlayer.getName() + ")", e);

                return null;
            });
        }

        playerMap.clear();
    }

    private void updateUserName(@NotNull BoxUser user) {
        userManager.saveUser(user)
                .exceptionallyAsync(e -> {
                    BoxProvider.get().getLogger().log(Level.SEVERE,
                            "Could not save the uuid and name of player (" + user.getName() + ")", e);
                    return null;
                });
    }

    private @NotNull CompletableFuture<Void> unloadAndSave(@NotNull BoxPlayer boxPlayer) {
        BoxProvider.get().getEventBus().callEvent(new PlayerUnloadEvent(boxPlayer));

        var stockHolder = boxPlayer.getUserStockHolder();

        if (stockHolder instanceof UserStockHolderLoader loader) {
            if (!loader.isLoaded()) {
                return CompletableFuture.completedFuture(null);
            }

            stockHolder = loader.getSource();
            loader.unload();
        }

        return stockManager.saveUserStock(stockHolder);
    }
}
