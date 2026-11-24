package net.okocraft.box.feature.command.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockActor;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class for implementing {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause}.
 */
public final class CommandCauses {

    public static StockEvent.@NotNull Cause deposit(@NotNull Player player) {
        return StockEvent.Cause.create("deposit", StockActor.fromPlayer(player));
    }

    public static StockEvent.@NotNull Cause withdraw(@NotNull Player player) {
        return StockEvent.Cause.create("withdraw", StockActor.fromPlayer(player));
    }

    /**
     * Captures the sender who confirmed a full reset.
     *
     * @param sender the confirming command sender
     * @return the full reset cause, distinct from a single-item reset
     */
    public static StockEvent.@NotNull Cause adminResetAll(@NotNull CommandSender sender) {
        return StockEvent.Cause.create("admin_reset_all", StockActor.fromSender(sender));
    }

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} implementation that indicates
     * giving items to the other player.
     *
     * @param target the {@link BoxPlayer} receiving the items
     * @param actor  the initiating player
     */
    public record Give(@NotNull BoxPlayer target, StockActor.@NotNull Player actor) implements StockEvent.Cause {
        public Give {
            Objects.requireNonNull(actor, "actor");
        }

        @Override
        public @NotNull String name() {
            return "give";
        }
    }

    /**
     * A {@link net.okocraft.box.api.event.stockholder.stock.StockEvent.Cause} implementation that indicates
     * receiving items from the other player.
     *
     * @param sender the {@link BoxPlayer} giving the items
     * @param actor  the initiating player
     */
    public record Receive(@NotNull BoxPlayer sender, StockActor.@NotNull Player actor) implements StockEvent.Cause {
        public Receive {
            Objects.requireNonNull(actor, "actor");
        }

        public Receive(@NotNull BoxPlayer sender) {
            this(sender, StockActor.fromPlayer(sender.getPlayer()));
        }

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
     * @param actor the executor's captured identity
     */
    public record AdminGive(@NotNull CommandSender sender, @NotNull StockActor actor) implements StockEvent.Cause {
        public AdminGive {
            Objects.requireNonNull(actor, "actor");
        }

        public AdminGive(@NotNull CommandSender sender) {
            this(sender, StockActor.fromSender(sender));
        }

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
     * @param actor the executor's captured identity
     */
    public record AdminReset(@NotNull CommandSender sender, @NotNull StockActor actor) implements StockEvent.Cause {
        public AdminReset {
            Objects.requireNonNull(actor, "actor");
        }

        public AdminReset(@NotNull CommandSender sender) {
            this(sender, StockActor.fromSender(sender));
        }

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
     * @param actor the executor's captured identity
     */
    public record AdminSet(@NotNull CommandSender sender, @NotNull StockActor actor) implements StockEvent.Cause {
        public AdminSet {
            Objects.requireNonNull(actor, "actor");
        }

        public AdminSet(@NotNull CommandSender sender) {
            this(sender, StockActor.fromSender(sender));
        }

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
     * @param actor the executor's captured identity
     */
    public record AdminTake(@NotNull CommandSender sender, @NotNull StockActor actor) implements StockEvent.Cause {
        public AdminTake {
            Objects.requireNonNull(actor, "actor");
        }

        public AdminTake(@NotNull CommandSender sender) {
            this(sender, StockActor.fromSender(sender));
        }

        @Override
        public @NotNull String name() {
            return "admin_take";
        }
    }

    private CommandCauses() {
        throw new UnsupportedOperationException();
    }

}
