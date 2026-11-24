package net.okocraft.box.api.event.stockholder.stock;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * The known initiator of a stock change, independent of the stock holder.
 */
public sealed interface StockActor permits StockActor.Player, StockActor.Console {

    record Player(@NotNull UUID uuid) implements StockActor {
        public Player {
            Objects.requireNonNull(uuid);
        }
    }

    enum Console implements StockActor {
        INSTANCE
    }

    /**
     * Identifies a player or console command sender. Other senders remain unattributed.
     */
    static @Nullable StockActor fromSender(@NotNull CommandSender sender) {
        return switch (sender) {
            case org.bukkit.entity.Player player -> new Player(player.getUniqueId());
            case ConsoleCommandSender ignored -> Console.INSTANCE;
            case RemoteConsoleCommandSender ignored -> Console.INSTANCE;
            default -> null;
        };
    }
}
