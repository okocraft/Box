package net.okocraft.box.core.player;

import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

final class NotLoadedPlayer implements BoxPlayer {

    @Override
    public @NotNull UUID getUUID() {
        throw new IllegalStateException();
    }

    @Override
    public @NotNull String getName() {
        throw new IllegalStateException();
    }

    @Override
    public @NotNull BoxUser asUser() {
        throw new IllegalStateException();
    }

    @Override
    public @NotNull Player getPlayer() {
        throw new IllegalStateException();
    }

    @Override
    public @NotNull StockHolder getPersonalStockHolder() {
        throw new IllegalStateException();
    }

    @Override
    public @NotNull StockHolder getCurrentStockHolder() {
        throw new IllegalStateException();
    }

    @Override
    public void setCurrentStockHolder(@NotNull StockHolder stockHolder) {
        throw new IllegalStateException();
    }
}
