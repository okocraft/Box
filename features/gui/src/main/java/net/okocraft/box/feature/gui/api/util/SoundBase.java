package net.okocraft.box.feature.gui.api.util;

import io.papermc.paper.registry.keys.SoundEventKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record SoundBase(@NotNull Sound sound) {

    public static final SoundBase CLICK = SoundBase.builder().sound(SoundEventKeys.BLOCK_COMPARATOR_CLICK).pitch(1.5f).build();
    public static final SoundBase UNSUCCESSFUL = SoundBase.builder().sound(SoundEventKeys.ENTITY_ENDERMAN_TELEPORT).pitch(1.5f).build();

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public void play(@NotNull Player target) {
        target.playSound(this.sound, target.getX(), target.getY(), target.getZ());
    }

    public static class Builder {

        private Key sound;
        private Sound.Source source = Sound.Source.MASTER;
        private float volume = 100f;
        private float pitch = 1.0f;
        private long seed = 0;

        @Contract("_ -> this")
        public @NotNull Builder sound(@NotNull Key sound) {
            this.sound = sound;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder source(@NotNull Sound.Source source) {
            this.source = source;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder volume(float volume) {
            this.volume = volume;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder pitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        @Contract(value = " -> new", pure = true)
        public @NotNull SoundBase build() {
            return new SoundBase(
                Sound.sound()
                    .type(Objects.requireNonNull(this.sound, "sound not set"))
                    .source(Objects.requireNonNullElse(this.source, Sound.Source.MASTER))
                    .volume(this.volume)
                    .pitch(this.pitch)
                    .seed(this.seed)
                    .build()
            );
        }
    }
}
