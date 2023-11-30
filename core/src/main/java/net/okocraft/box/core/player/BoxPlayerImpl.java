package net.okocraft.box.core.player;

import net.okocraft.box.api.BoxProvider;
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

    private StockHolder currentHolder;

    public BoxPlayerImpl(@NotNull BoxUser user, @NotNull Player player, @NotNull LoadingPersonalStockHolder personalStockHolder) {
        this.user = user;
        this.player = player;
        this.personalStockHolder = personalStockHolder;
        this.currentHolder = personalStockHolder;
    }

    @Override
    public @NotNull BoxUser asUser() {
        return user;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull LoadingPersonalStockHolder getPersonalStockHolder() {
        return personalStockHolder;
    }

    @Override
    public @NotNull StockHolder getCurrentStockHolder() {
        return currentHolder;
    }

    @Override
    public void setCurrentStockHolder(@NotNull StockHolder stockHolder) {
        var previous = currentHolder;
        currentHolder = Objects.requireNonNull(stockHolder);
        BoxProvider.get().getEventBus().callEventAsync(new PlayerStockHolderChangeEvent(this, previous));
    }

    @Override
    public @NotNull UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public @NotNull String getName() {
        return player.getName();
    }
}
