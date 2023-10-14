package net.okocraft.box.core.player;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.player.PlayerLoadEvent;
import net.okocraft.box.api.event.player.PlayerUnloadEvent;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.core.message.ErrorMessages;
import net.okocraft.box.core.model.loader.UserStockHolderLoader;
import net.okocraft.box.core.model.manager.user.BoxUserManager;
import net.okocraft.box.core.util.executor.InternalExecutors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BoxPlayerMapImpl implements BoxPlayerMap {

    // This BoxPlayer indicates that the player data has not yet been loaded.
    // This is because ConcurrentHashMap does not allow null.
    private static final BoxPlayer NOT_LOADED_YET = new NotLoadedPlayer();

    private final Map<Player, BoxPlayer> playerMap = new ConcurrentHashMap<>();
    private final BoxUserManager userManager;
    private final StockManager stockManager;
    private final ScheduledExecutorService scheduler;

    public BoxPlayerMapImpl(@NotNull BoxUserManager userManager, @NotNull StockManager stockManager) {
        this.userManager = userManager;
        this.stockManager = stockManager;
        this.scheduler = InternalExecutors.newSingleThreadScheduler("Player Loader");
    }

    @Override
    public boolean isLoaded(@NotNull Player player) {
        var boxPlayer = playerMap.get(player);
        return boxPlayer != null && boxPlayer != NOT_LOADED_YET;
    }

    @Override
    public boolean isScheduledLoading(@NotNull Player player) {
        return playerMap.get(player) == NOT_LOADED_YET;
    }

    @Override
    public @NotNull BoxPlayer get(@NotNull Player player) {
        Objects.requireNonNull(player);
        var boxPlayer = playerMap.get(player);

        if (boxPlayer == null || boxPlayer == NOT_LOADED_YET) {
            throw new IllegalStateException("player is not loaded (" + player.getName() + ")");
        }

        return boxPlayer;
    }

    public void scheduleLoadingData(@NotNull Player player) {
        playerMap.put(player, NOT_LOADED_YET);
        scheduler.schedule(() -> load(player), 1, TimeUnit.SECONDS);
    }

    private void load(@NotNull Player player) {
        try {
            loadBoxPlayer(player);
        } catch (Exception e) {
            playerMap.remove(player);
            BoxProvider.get().getLogger().log(Level.SEVERE, "Could not load a player (" + player.getName() + ")", e);
            player.sendMessage(ErrorMessages.ERROR_LOAD_PLAYER_DATA_ON_JOIN);
        }
    }

    private void loadBoxPlayer(@NotNull Player player) {
        if (!player.isOnline()) { // The player is no longer online, so remove it from the map.
            playerMap.remove(player);
            return;
        }

        if (playerMap.get(player) != NOT_LOADED_YET) { // This prevents loading data twice.
            return;
        }

        var boxUser = this.userManager.createBoxUser(player.getUniqueId(), player.getName());

        this.userManager.saveUsername(boxUser);

        var userStock = stockManager.loadUserStock(boxUser).join();

        var boxPlayer = new BoxPlayerImpl(boxUser, player, userStock);

        playerMap.put(player, boxPlayer);

        BoxProvider.get().getEventBus().callEvent(new PlayerLoadEvent(boxPlayer));
    }

    public void unload(@NotNull Player player) {
        Objects.requireNonNull(player);
        var boxPlayer = playerMap.remove(player);

        if (boxPlayer != null && boxPlayer != NOT_LOADED_YET) {
            scheduler.execute(() -> unload0(boxPlayer));
        }
    }

    private void unload0(@NotNull BoxPlayer boxPlayer) {
        try {
            unloadAndSave(boxPlayer).join();
        } catch (Exception e) {
            if (boxPlayer.getPlayer().isOnline()) {
                boxPlayer.getPlayer().sendMessage(ErrorMessages.ERROR_SAVE_PLAYER_DATA);
            }

            BoxProvider.get().getLogger().log(Level.SEVERE, "Could not save player data (" + boxPlayer.getName() + ")", e);
        }
    }

    public void loadAll() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            playerMap.put(player, NOT_LOADED_YET);
            load(player);
        });
    }

    public void unloadAll() {
        List.copyOf(playerMap.keySet())
                .stream()
                .map(playerMap::remove)
                .filter(Objects::nonNull)
                .forEach(this::unload0);
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
