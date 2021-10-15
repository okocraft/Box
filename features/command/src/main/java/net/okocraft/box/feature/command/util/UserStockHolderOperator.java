package net.okocraft.box.feature.command.util;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.stock.UserStockHolder;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class UserStockHolderOperator {

    private final String argument;
    private boolean supportOffline = false;
    private Consumer<UserStockHolder> stockHolderConsumer;
    private Consumer<String> argumentConsumer;

    private UserStockHolderOperator(@NotNull String argument) {
        this.argument = argument;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull UserStockHolderOperator create(@NotNull String argument) {
        return new UserStockHolderOperator(argument);
    }

    @Contract(value = "_ -> this")
    public @NotNull UserStockHolderOperator supportOffline(boolean bool) {
        supportOffline = bool;
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull UserStockHolderOperator stockHolderOperator(@NotNull Consumer<UserStockHolder> stockHolderConsumer) {
        this.stockHolderConsumer = stockHolderConsumer;
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull UserStockHolderOperator onNotFound(@NotNull Consumer<String> argumentConsumer) {
        this.argumentConsumer = argumentConsumer;
        return this;
    }

    public void run() {
        if (stockHolderConsumer == null) {
            return;
        }

        var target = getUserStockHolder();

        if (target != null) {
            stockHolderConsumer.accept(target);
        } else {
            argumentConsumer.accept(argument);
        }
    }

    private @Nullable UserStockHolder getUserStockHolder() {
        UUID uuid = null;

        try {
            uuid = UUID.fromString(argument);
        } catch (IllegalArgumentException ignored) {
        }

        var player = uuid != null ? Bukkit.getPlayer(uuid) : Bukkit.getPlayer(argument);
        var playerMap = BoxProvider.get().getBoxPlayerMap();

        if (player != null && playerMap.isLoaded(player)) {
            return playerMap.get(player).getUserStockHolder();
        }

        if (supportOffline) {
            return getOfflineUserStockHolder(uuid);
        } else {
            return null;
        }
    }

    private @Nullable UserStockHolder getOfflineUserStockHolder(@Nullable UUID uuid) {
        var boxUser = uuid != null ?
                BoxProvider.get().getUserManager().loadUser(uuid).join() :
                BoxProvider.get().getUserManager().search(argument).join().orElse(null);

        var stockManager = BoxProvider.get().getStockManager();

        return boxUser != null ? stockManager.loadUserStock(boxUser).join() : null;
    }
}
