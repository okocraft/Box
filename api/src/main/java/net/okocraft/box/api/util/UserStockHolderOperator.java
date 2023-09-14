package net.okocraft.box.api.util;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.stock.UserStockHolder;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * A utility class to get the {@link UserStockHolder}. and do something with it.
 *
 * @deprecated {@link UserStockHolder} will be removed in Box 6.0.0
 */
@Deprecated(since = "5.5.0", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
public final class UserStockHolderOperator {

    private final String argument;
    private boolean supportOffline = false;
    private Consumer<UserStockHolder> stockHolderConsumer;
    private Consumer<String> argumentConsumer;

    private UserStockHolderOperator(@NotNull String argument) {
        this.argument = argument;
    }

    /**
     * Creates a new {@link UserStockHolderOperator}.
     *
     * @param argument the argument
     * @return a new {@link UserStockHolderOperator}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull UserStockHolderOperator create(@NotNull String argument) {
        return new UserStockHolderOperator(argument);
    }

    /**
     * Sets whether to support offline players.
     *
     * @param bool {@code true} to support offline players, {@code false} otherwise
     * @return this builder
     */
    @Contract(value = "_ -> this")
    public @NotNull UserStockHolderOperator supportOffline(boolean bool) {
        supportOffline = bool;
        return this;
    }

    /**
     * Sets the {@link Consumer} of the {@link UserStockHolder}.
     *
     * @param stockHolderConsumer the {@link Consumer} of the {@link UserStockHolder}
     * @return this builder
     */
    @Contract(value = "_ -> this")
    public @NotNull UserStockHolderOperator stockHolderOperator(@NotNull Consumer<UserStockHolder> stockHolderConsumer) {
        this.stockHolderConsumer = stockHolderConsumer;
        return this;
    }

    /**
     * Sets the {@link Consumer} of the argument that is called when the player is not found.
     *
     * @param argumentConsumer the {@link Consumer} of the argument
     * @return this builder
     */
    @Contract(value = "_ -> this")
    public @NotNull UserStockHolderOperator onNotFound(@NotNull Consumer<String> argumentConsumer) {
        this.argumentConsumer = argumentConsumer;
        return this;
    }

    /**
     * Runs this operator.
     */
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

    /**
     * Gets the search result of the {@link UserStockHolder}.
     *
     * @return the search result of the {@link UserStockHolder}
     */
    public @Nullable UserStockHolder getUserStockHolder() {
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
