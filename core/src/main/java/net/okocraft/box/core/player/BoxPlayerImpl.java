package net.okocraft.box.core.player;

import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class BoxPlayerImpl implements BoxPlayer {

    private final Player player;
    private final UserStockHolder userStockHolder;

    public BoxPlayerImpl(@NotNull Player player, @NotNull UserStockHolder userStockHolder) {
        this.player = player;
        this.userStockHolder = userStockHolder;
    }

    @Override
    public @NotNull UserStockHolder getUserStockHolder() {
        return userStockHolder;
    }

    @Override
    public @NotNull StockHolder getCurrentStockHolder() {
        return userStockHolder;
    }

    @Override
    public void setCurrentStockHolder(@NotNull StockHolder stockHolder) {
        Objects.requireNonNull(stockHolder);
    }

    @Override
    public @NotNull UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public @NotNull Optional<String> getName() {
        return Optional.of(player.getName());
    }
}
