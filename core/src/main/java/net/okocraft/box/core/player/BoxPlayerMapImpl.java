package net.okocraft.box.core.player;

import net.okocraft.box.api.event.caller.EventCallerProvider;
import net.okocraft.box.api.event.player.PlayerLoadEvent;
import net.okocraft.box.api.event.player.PlayerUnloadEvent;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.core.message.CoreMessages;
import net.okocraft.box.core.model.loader.LoadingPersonalStockHolder;
import net.okocraft.box.core.model.manager.stock.BoxStockManager;
import net.okocraft.box.core.model.manager.user.BoxUserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BoxPlayerMapImpl implements BoxPlayerMap {

    // This BoxPlayer indicates that the player data has not yet been loaded.
    // This is because ConcurrentHashMap does not allow null.
    private static final BoxPlayer NOT_LOADED_YET = new NotLoadedPlayer();

    private final Map<Player, BoxPlayer> playerMap = new ConcurrentHashMap<>();
    private final BoxStockManager stockManager;
    private final BoxUserManager userManager;
    private final EventCallerProvider eventCallers;
    private final BoxScheduler scheduler;

    public BoxPlayerMapImpl(@NotNull BoxUserManager userManager, @NotNull BoxStockManager stockManager,
                            @NotNull EventCallerProvider eventCallers, @NotNull BoxScheduler scheduler) {
        this.userManager = userManager;
        this.stockManager = stockManager;
        this.eventCallers = eventCallers;
        this.scheduler = scheduler;
    }

    @Override
    public boolean isLoaded(@NotNull Player player) {
        BoxPlayer boxPlayer = this.playerMap.get(player);
        return boxPlayer != null && boxPlayer != NOT_LOADED_YET;
    }

    @Override
    public boolean isScheduledLoading(@NotNull Player player) {
        return this.playerMap.get(player) == NOT_LOADED_YET;
    }

    @Override
    public @NotNull BoxPlayer get(@NotNull Player player) {
        Objects.requireNonNull(player);
        BoxPlayer boxPlayer = this.playerMap.get(player);

        if (boxPlayer == null || boxPlayer == NOT_LOADED_YET) {
            throw new IllegalStateException("player is not loaded (" + player.getName() + ")");
        }

        return boxPlayer;
    }

    public void scheduleLoadingData(@NotNull Player player) {
        if (this.playerMap.put(player, NOT_LOADED_YET) == NOT_LOADED_YET) {
            return;
        }

        this.scheduler.scheduleAsyncTask(() -> this.load(player), 1, TimeUnit.SECONDS);
    }

    private void load(@NotNull Player player) {
        try {
            this.loadBoxPlayer(player);
        } catch (Exception e) {
            this.playerMap.remove(player);
            BoxLogger.logger().error("Could not load a player ({})", player.getName(), e);
            player.sendMessage(CoreMessages.LOAD_FAILURE_ON_JOIN);
        }
    }

    private void loadBoxPlayer(@NotNull Player player) {
        if (!player.isOnline()) { // The player is no longer online, so remove it from the map.
            this.playerMap.remove(player);
            return;
        }

        BoxUser boxUser = this.userManager.createBoxUser(player.getUniqueId(), player.getName());
        LoadingPersonalStockHolder personal = this.stockManager.getPersonalStockHolder(boxUser);
        BoxPlayerImpl boxPlayer = new BoxPlayerImpl(boxUser, player, personal, this.eventCallers.sync());

        if (this.playerMap.replace(player, NOT_LOADED_YET, boxPlayer)) { // This prevents loading data twice.
            personal.load();
            personal.markAsOnline();

            this.userManager.saveUsername(boxUser);
            this.eventCallers.async().call(new PlayerLoadEvent(boxPlayer));
        }
    }

    public void unload(@NotNull Player player) {
        if (this.playerMap.get(Objects.requireNonNull(player)) instanceof BoxPlayerImpl boxPlayer) {
            this.unload(boxPlayer);
        }
    }

    private void unload(@NotNull BoxPlayerImpl boxPlayer) {
        boxPlayer.getPersonalStockHolder().markAsOffline();
        this.eventCallers.async().call(new PlayerUnloadEvent(boxPlayer));
    }

    public void loadAll() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            this.playerMap.put(player, NOT_LOADED_YET);
            this.load(player);
        });
    }

    public void unloadAll() {
        Iterator<BoxPlayer> iterator = this.playerMap.values().iterator();

        while (iterator.hasNext()) {
            BoxPlayer player = iterator.next();
            iterator.remove();

            if (player instanceof BoxPlayerImpl boxPlayer) {
                this.unload(boxPlayer);
            }
        }
    }
}
