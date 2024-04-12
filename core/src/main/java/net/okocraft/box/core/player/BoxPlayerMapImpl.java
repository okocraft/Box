package net.okocraft.box.core.player;

import com.github.siroshun09.event4j.caller.AsyncEventCaller;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.player.PlayerLoadEvent;
import net.okocraft.box.api.event.player.PlayerUnloadEvent;
import net.okocraft.box.api.message.MessageProvider;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.core.message.CoreMessages;
import net.okocraft.box.core.model.manager.stock.BoxStockManager;
import net.okocraft.box.core.model.manager.user.BoxUserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
    private final AsyncEventCaller<BoxEvent> eventCaller;
    private final BoxScheduler scheduler;
    private final MessageProvider messageProvider;

    public BoxPlayerMapImpl(@NotNull BoxUserManager userManager, @NotNull BoxStockManager stockManager,
                            @NotNull AsyncEventCaller<BoxEvent> eventCaller, @NotNull BoxScheduler scheduler,
                            @NotNull MessageProvider messageProvider) {
        this.userManager = userManager;
        this.stockManager = stockManager;
        this.eventCaller = eventCaller;
        this.scheduler = scheduler;
        this.messageProvider = messageProvider;
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
        if (this.playerMap.put(player, NOT_LOADED_YET) == NOT_LOADED_YET) {
            return;
        }

        this.scheduler.scheduleAsyncTask(() -> this.load(player), 1, TimeUnit.SECONDS);
    }

    private void load(@NotNull Player player) {
        try {
            loadBoxPlayer(player);
        } catch (Exception e) {
            playerMap.remove(player);
            BoxLogger.logger().error("Could not load a player ({})", player.getName(), e);
            CoreMessages.LOAD_FAILURE_ON_JOIN.source(this.messageProvider.findSource(player)).send(player);
        }
    }

    private void loadBoxPlayer(@NotNull Player player) {
        if (!player.isOnline()) { // The player is no longer online, so remove it from the map.
            playerMap.remove(player);
            return;
        }

        var boxUser = this.userManager.createBoxUser(player.getUniqueId(), player.getName());
        var personal = this.stockManager.getPersonalStockHolder(boxUser);
        var boxPlayer = new BoxPlayerImpl(boxUser, player, personal, this.eventCaller);

        if (this.playerMap.replace(player, NOT_LOADED_YET, boxPlayer)) { // This prevents loading data twice.
            personal.load();
            personal.markAsOnline();

            this.userManager.saveUsername(boxUser);
            this.eventCaller.call(new PlayerLoadEvent(boxPlayer));
        }
    }

    public void unload(@NotNull Player player) {
        Objects.requireNonNull(player);

        if (!(this.playerMap instanceof BoxPlayerImpl boxPlayer)) {
            return;
        }

        boxPlayer.getPersonalStockHolder().markAsOffline();

        this.eventCaller.call(new PlayerUnloadEvent(boxPlayer));
    }

    public void loadAll() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            this.playerMap.put(player, NOT_LOADED_YET);
            load(player);
        });
    }

    public void unloadAll() {
        playerMap.clear();
    }
}
