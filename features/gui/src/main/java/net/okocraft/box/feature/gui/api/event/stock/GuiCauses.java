package net.okocraft.box.feature.gui.api.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockActor;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class for implementing {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause}.
 */
public final class GuiCauses {

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} that indicates depositing to own {@link net.okocraft.box.api.model.stock.StockHolder}.
     *
     * @param clicker the {@link Player} who click the button in GUI
     * @param actor the clicker's captured identity
     */
    public record Deposit(@NotNull Player clicker, StockActor.@NotNull Player actor) implements StockEvent.Cause {
        public Deposit {
            Objects.requireNonNull(actor, "actor");
        }

        public Deposit(@NotNull Player clicker) {
            this(clicker, StockActor.fromPlayer(clicker));
        }

        @Override
        public @NotNull String name() {
            return "deposit";
        }
    }

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} that indicates withdrawing from own {@link net.okocraft.box.api.model.stock.StockHolder}.
     *
     * @param clicker the {@link Player} who click the button in GUI
     * @param actor the clicker's captured identity
     */
    public record Withdraw(@NotNull Player clicker, StockActor.@NotNull Player actor) implements StockEvent.Cause {
        public Withdraw {
            Objects.requireNonNull(actor, "actor");
        }

        public Withdraw(@NotNull Player clicker) {
            this(clicker, StockActor.fromPlayer(clicker));
        }

        @Override
        public @NotNull String name() {
            return "withdraw";
        }
    }

    private GuiCauses() {
        throw new UnsupportedOperationException();
    }
}
