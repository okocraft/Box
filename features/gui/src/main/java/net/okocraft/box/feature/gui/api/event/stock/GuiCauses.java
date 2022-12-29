package net.okocraft.box.feature.gui.api.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class GuiCauses {

    public record Deposit(@NotNull Player clicker) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "deposit";
        }
    }

    public record Withdraw(@NotNull Player clicker) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "withdraw";
        }
    }

    private GuiCauses() {
        throw new UnsupportedOperationException();
    }
}
