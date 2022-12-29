package net.okocraft.box.feature.gui.api.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A class for implementing {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause}.
 */
public final class GuiCauses {

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} that indicates depositing to own {@link net.okocraft.box.api.model.stock.StockHolder}.
     *
     * @param clicker the {@link Player} who click the button in GUI
     */
    public record Deposit(@NotNull Player clicker) implements StockEvent.Cause {
        @Override
        public @NotNull String name() {
            return "deposit";
        }
    }

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} that indicates withdrawing from own {@link net.okocraft.box.api.model.stock.StockHolder}.
     *
     * @param clicker the {@link Player} who click the button in GUI
     */
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
