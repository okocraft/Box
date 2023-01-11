package net.okocraft.box.feature.stick.event.stock;

import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * A class holdings {@link Record} classes that implements {@link StickCause}.
 */
public final class StickCauses {

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used for the furnace.
     *
     * @param player          the player that used the Box Stick
     * @param furnaceLocation the {@link Location} of the furnace where the Box Stick was used
     * @param type            the function used for the furnace
     */
    public record Furnace(@NotNull BoxPlayer player, @NotNull Location furnaceLocation,
                          @NotNull Type type) implements StickCause {
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
     */
    public record Brewer(@NotNull BoxPlayer player, @NotNull Location brewerLocation,
                         @NotNull Type type) implements StickCause {
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
     */
    public record Container(@NotNull BoxPlayer player, @NotNull Location containerLocation) implements StickCause {
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
     */
    public record BlockPlace(@NotNull BoxPlayer player, @NotNull Location placedLocation) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_block_place";
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used when the player consumed the items.
     *
     * @param player the player that used the Box Stick
     */
    public record ItemConsume(@NotNull BoxPlayer player) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_item_consume";
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used when the player broken the items.
     *
     * @param player the player that used the Box Stick
     */
    public record ItemBreak(@NotNull BoxPlayer player) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_item_break";
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used when the player launched the projectile.
     *
     * @param player the player that used the Box Stick
     * @param entityType the {@link EntityType} that the player launched
     */
    public record ProjectileLaunch(@NotNull BoxPlayer player, @NotNull EntityType entityType) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_projectile_launch";
        }
    }

    /**
     * A record of {@link StickCause} indicating that a Box Stick was used when the player shot the arrow using the bow.
     *
     * @param player the player that used the Box Stick
     */
    public record ShootBow(@NotNull BoxPlayer player) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_shoot_bow";
        }
    }
}
