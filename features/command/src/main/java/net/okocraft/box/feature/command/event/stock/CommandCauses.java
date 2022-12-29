package net.okocraft.box.feature.command.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class CommandCauses {

    public static final StockEvent.Cause DEPOSIT = StockEvent.Cause.create("deposit");
    public static final StockEvent.Cause WITHDRAW = StockEvent.Cause.create("withdraw");

    public record Give(@NotNull BoxPlayer target) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "give";
        }
    }

    public record Receive(@NotNull BoxPlayer sender) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "receive";
        }
    }

    public record AdminGive(@NotNull CommandSender sender) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "admin_give";
        }
    }

    public record AdminReset(@NotNull CommandSender sender) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "admin_reset";
        }
    }

    public record AdminSet(@NotNull CommandSender sender) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "admin_set";
        }
    }

    public record AdminTake(@NotNull CommandSender sender) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "admin_take";
        }
    }

}
