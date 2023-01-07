package net.okocraft.box.feature.stick.event.stock;

import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public final class StickCauses {

    public record Furnace(@NotNull BoxPlayer player, @NotNull Location furnaceLocation,
                          @NotNull Type type) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_furnace";
        }

        public enum Type {
            TAKE_RESULT_ITEM,
            PUT_INGREDIENT,
            PUT_FUEL
        }
    }

    public record Brewer(@NotNull BoxPlayer player, @NotNull Location brewerLocation,
                         @NotNull Type type) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_brewer";
        }

        public enum Type {
            TAKE_POTION,
            PUT_POTION,
            PUT_BLAZE_POWDER
        }
    }

    public record Container(@NotNull BoxPlayer player, @NotNull Location containerLocation) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_container";
        }
    }

    public record BlockPlace(@NotNull BoxPlayer player, @NotNull Location placedLocation) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_block_place";
        }
    }

    public record ItemConsume(@NotNull BoxPlayer player) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_item_consume";
        }
    }

    public record ItemBreak(@NotNull BoxPlayer player) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_item_break";
        }
    }

    public record ProjectileLaunch(@NotNull BoxPlayer player, @NotNull EntityType type) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_projectile_launch";
        }
    }

    public record ShootBow(@NotNull BoxPlayer player) implements StickCause {
        @Override
        public @NotNull String name() {
            return "stick_shoot_bow";
        }
    }
}
