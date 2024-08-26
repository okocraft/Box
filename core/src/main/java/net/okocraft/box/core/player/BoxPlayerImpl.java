package net.okocraft.box.core.player;

import dev.siroshun.event4j.api.caller.EventCaller;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.player.PlayerStockHolderChangeEvent;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.core.model.loader.LoadingPersonalStockHolder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class BoxPlayerImpl implements BoxPlayer {

    private final BoxUser user;
    private final Player player;
    private final LoadingPersonalStockHolder personalStockHolder;

    private volatile StockHolder currentHolder;
    private final EventCaller<BoxEvent> eventCaller;

    public BoxPlayerImpl(@NotNull BoxUser user, @NotNull Player player,
                         @NotNull LoadingPersonalStockHolder personalStockHolder, @NotNull EventCaller<BoxEvent> eventCaller) {
        this.user = user;
        this.player = player;
        this.personalStockHolder = personalStockHolder;
        this.currentHolder = personalStockHolder;
        this.eventCaller = eventCaller;
    }

    @Override
    public @NotNull BoxUser asUser() {
        return this.user;
    }

    @Override
    public @NotNull Player getPlayer() {
        return this.player;
    }

    @Override
    public @NotNull LoadingPersonalStockHolder getPersonalStockHolder() {
        return this.personalStockHolder;
    }

    @Override
    public @NotNull StockHolder getCurrentStockHolder() {
        return this.currentHolder;
    }

    @Override
    public void setCurrentStockHolder(@NotNull StockHolder stockHolder) {
        var previous = this.currentHolder;
        this.currentHolder = Objects.requireNonNull(stockHolder);
        this.eventCaller.call(new PlayerStockHolderChangeEvent(this, previous));
    }

    @Override
    public @NotNull UUID getUUID() {
        return this.player.getUniqueId();
    }

    @Override
    public @NotNull String getName() {
        return this.player.getName();
    }
}
