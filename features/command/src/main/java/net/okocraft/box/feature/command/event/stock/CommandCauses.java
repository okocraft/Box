package net.okocraft.box.feature.command.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * A class for implementing {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause}.
 */
public final class CommandCauses {

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} that indicates depositing to own {@link net.okocraft.box.api.model.stock.StockHolder}.
     */
    public static final StockEvent.Cause DEPOSIT = StockEvent.Cause.create("deposit");

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} that indicates withdrawing from  own {@link net.okocraft.box.api.model.stock.StockHolder}.
     */
    public static final StockEvent.Cause WITHDRAW = StockEvent.Cause.create("withdraw");

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} implementation that indicates
     * giving items to the other player.
     *
     * @param target the {@link  BoxPlayer} who receive items
     */
    public record Give(@NotNull BoxPlayer target) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "give";
        }
    }

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} implementation that indicates
     * receiving items from the other player.
     *
     * @param sender the {@link  BoxPlayer} who give items
     */
    public record Receive(@NotNull BoxPlayer sender) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "receive";
        }
    }

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} implementation that indicates
     * increasing stock by an admin.
     *
     * @param sender executor of the command to increase stock
     */
    public record AdminGive(@NotNull CommandSender sender) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "admin_give";
        }
    }

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} implementation that indicates
     * resetting stock by an admin.
     *
     * @param sender executor of the command to reset stock
     */
    public record AdminReset(@NotNull CommandSender sender) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "admin_reset";
        }
    }

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} implementation that indicates
     * setting stock by an admin.
     *
     * @param sender executor of the command to set stock
     */
    public record AdminSet(@NotNull CommandSender sender) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "admin_set";
        }
    }

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} implementation that indicates
     * decreasing stock by an admin.
     *
     * @param sender executor of the command to decrease stock
     */
    public record AdminTake(@NotNull CommandSender sender) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "admin_take";
        }
    }

    private CommandCauses() {
        throw new UnsupportedOperationException();
    }

}
