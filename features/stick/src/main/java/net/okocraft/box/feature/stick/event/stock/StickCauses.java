package net.okocraft.box.feature.stick.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockActor;
import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class holdings {@link Record} classes that implements {@link StickCause}.
 */
public final class StickCauses {

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used for getting the item of the block.
     *
     * @param player        the player that used the Box Stick
     * @param blockLocation the {@link Location} of the block where the Box Stick was used
     * @param actor         the player's captured identity
     */
    public record BlockItem(@NotNull BoxPlayer player, @NotNull Location blockLocation,
                            StockActor.@NotNull Player actor) implements StickCause {
        public BlockItem {
            Objects.requireNonNull(actor, "actor");
        }

        public BlockItem(@NotNull BoxPlayer player, @NotNull Location blockLocation) {
            this(player, blockLocation, captureActor(player));
        }

        @Override
        public @NotNull String name() {
            return "stick_block_item";
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used for the furnace.
     *
     * @param player          the player that used the Box Stick
     * @param furnaceLocation the {@link Location} of the furnace where the Box Stick was used
     * @param type            the function used for the furnace
     * @param actor           the player's captured identity
     */
    public record Furnace(@NotNull BoxPlayer player, @NotNull Location furnaceLocation,
                          @NotNull Type type, StockActor.@NotNull Player actor) implements StickCause {
        public Furnace {
            Objects.requireNonNull(actor, "actor");
        }

        public Furnace(@NotNull BoxPlayer player, @NotNull Location furnaceLocation, @NotNull Type type) {
            this(player, furnaceLocation, type, captureActor(player));
        }

        @Override
        public @NotNull String name() {
            return "stick_furnace";
        }

        /**
         * Functions that can be used for the furnace.
         */
        public enum Type {
            /**
             * Takes the result item out from the furnace.
             */
            TAKE_RESULT_ITEM,
            /**
             * Puts the ingredients into the furnace.
             */
            PUT_INGREDIENT,
            /**
             * Puts the fuels into the furnace.
             */
            PUT_FUEL
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used for the brewer.
     *
     * @param player         the player that used the Box Stick
     * @param brewerLocation the {@link Location} of the brewer where the Box Stick was used
     * @param type           the function used for the brewer
     * @param actor          the player's captured identity
     */
    public record Brewer(@NotNull BoxPlayer player, @NotNull Location brewerLocation,
                         @NotNull Type type, StockActor.@NotNull Player actor) implements StickCause {
        public Brewer {
            Objects.requireNonNull(actor, "actor");
        }

        public Brewer(@NotNull BoxPlayer player, @NotNull Location brewerLocation, @NotNull Type type) {
            this(player, brewerLocation, type, captureActor(player));
        }

        @Override
        public @NotNull String name() {
            return "stick_brewer";
        }

        /**
         * Functions that can be used for the brewer.
         */
        public enum Type {
            /**
             * Takes the potions out from the brewer.
             */
            TAKE_POTION,
            /**
             * Puts the potions into the brewer.
             */
            PUT_POTION,
            /**
             * Puts the blaze powders into the brewer.
             */
            PUT_BLAZE_POWDER
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used for the container.
     *
     * @param player            the player that used the Box Stick
     * @param containerLocation the {@link Location} of the container where the Box Stick was used
     * @param actor             the player's captured identity
     */
    public record Container(@NotNull BoxPlayer player, @NotNull Location containerLocation,
                            StockActor.@NotNull Player actor) implements StickCause {
        public Container {
            Objects.requireNonNull(actor, "actor");
        }

        public Container(@NotNull BoxPlayer player, @NotNull Location containerLocation) {
            this(player, containerLocation, captureActor(player));
        }

        @Override
        public @NotNull String name() {
            return "stick_container";
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used when the player placed the block.
     *
     * @param player         the player that used the Box Stick
     * @param placedLocation the {@link Location} of the block where the player placed
     * @param actor          the player's captured identity
     */
    public record BlockPlace(@NotNull BoxPlayer player, @NotNull Location placedLocation,
                             StockActor.@NotNull Player actor) implements StickCause {
        public BlockPlace {
            Objects.requireNonNull(actor, "actor");
        }

        public BlockPlace(@NotNull BoxPlayer player, @NotNull Location placedLocation) {
            this(player, placedLocation, captureActor(player));
        }

        @Override
        public @NotNull String name() {
            return "stick_block_place";
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used when the player consumed the items.
     *
     * @param player the player that used the Box Stick
     * @param actor  the player's captured identity
     */
    public record ItemConsume(@NotNull BoxPlayer player, StockActor.@NotNull Player actor) implements StickCause {
        public ItemConsume {
            Objects.requireNonNull(actor, "actor");
        }

        public ItemConsume(@NotNull BoxPlayer player) {
            this(player, captureActor(player));
        }

        @Override
        public @NotNull String name() {
            return "stick_item_consume";
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used when the player broken the items.
     *
     * @param player the player that used the Box Stick
     * @param actor  the player's captured identity
     */
    public record ItemBreak(@NotNull BoxPlayer player, StockActor.@NotNull Player actor) implements StickCause {
        public ItemBreak {
            Objects.requireNonNull(actor, "actor");
        }

        public ItemBreak(@NotNull BoxPlayer player) {
            this(player, captureActor(player));
        }

        @Override
        public @NotNull String name() {
            return "stick_item_break";
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used when the player launched the projectile.
     *
     * @param player     the player that used the Box Stick
     * @param entityType the {@link EntityType} that the player launched
     * @param actor      the player's captured identity
     */
    public record ProjectileLaunch(@NotNull BoxPlayer player, @NotNull EntityType entityType,
                                   StockActor.@NotNull Player actor) implements StickCause {
        public ProjectileLaunch {
            Objects.requireNonNull(actor, "actor");
        }

        public ProjectileLaunch(@NotNull BoxPlayer player, @NotNull EntityType entityType) {
            this(player, entityType, captureActor(player));
        }

        @Override
        public @NotNull String name() {
            return "stick_projectile_launch";
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used when the player shot the arrow using the bow.
     *
     * @param player the player that used the Box Stick
     * @param actor  the player's captured identity
     */
    public record ShootBow(@NotNull BoxPlayer player, StockActor.@NotNull Player actor) implements StickCause {
        public ShootBow {
            Objects.requireNonNull(actor, "actor");
        }

        public ShootBow(@NotNull BoxPlayer player) {
            this(player, captureActor(player));
        }

        @Override
        public @NotNull String name() {
            return "stick_shoot_bow";
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used when the player is resurrecting using the totem of undying.
     *
     * @param player the player that used the Box Stick
     * @param actor  the player's captured identity
     */
    public record Resurrect(@NotNull BoxPlayer player, StockActor.@NotNull Player actor) implements StickCause {
        public Resurrect {
            Objects.requireNonNull(actor, "actor");
        }

        public Resurrect(@NotNull BoxPlayer player) {
            this(player, captureActor(player));
        }

        @Override
        public @NotNull String name() {
            return "stick_resurrect";
        }
    }

    private static StockActor.@NotNull Player captureActor(@NotNull BoxPlayer player) {
        return StockActor.fromPlayer(player.getPlayer());
    }

    private StickCauses() {
        throw new UnsupportedOperationException();
    }
}
