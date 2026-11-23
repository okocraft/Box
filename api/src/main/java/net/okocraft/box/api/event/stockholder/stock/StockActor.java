package net.okocraft.box.api.event.stockholder.stock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * The initiator of a stock change, independent of the stock holder.
 * Implementations should supply captured values rather than consult live server state,
 * because stock events may be delivered asynchronously.
 */
public interface StockActor {

    /**
     * The built-in actor for the server console.
     */
    public static final StockActor CONSOLE = new Other("console");

    /**
     * Returns the actor's UUID, or {@code null} if it has none.
     * A player actor always has a UUID, but other actors may have one too.
     *
     * @return the actor's UUID, if available
     */
    @Nullable UUID uuid();

    /**
     * Returns the actor's name when the change was initiated.
     *
     * @return the actor's name
     */
    @NotNull String name();

    /**
     * Returns whether the actor represents a player.
     *
     * @return whether this is a player actor
     */
    default boolean isPlayer() {
        return false;
    }

    /**
     * An immutable player identity.
     *
     * @param uuid the player's UUID
     * @param name the player's name
     */
    record Player(@NotNull UUID uuid, @NotNull String name) implements StockActor {
        public Player {
            Objects.requireNonNull(uuid, "uuid");
            Objects.requireNonNull(name, "name");
        }

        @Override
        public boolean isPlayer() {
            return true;
        }
    }

    /**
     * An immutable non-player identity.
     *
     * @param uuid the actor's UUID, if available
     * @param name the actor's name
     */
    record Other(@Nullable UUID uuid, @NotNull String name) implements StockActor {
        public Other {
            Objects.requireNonNull(name, "name");
        }

        public Other(@NotNull String name) {
            this(null, name);
        }
    }
}
