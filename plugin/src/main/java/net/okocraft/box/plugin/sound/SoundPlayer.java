package net.okocraft.box.plugin.sound;

import net.okocraft.box.plugin.Box;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SoundPlayer {

    private final Box plugin;
    private final Random random;

    public SoundPlayer(@NotNull Box plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public void play(@NotNull Player target, @NotNull BoxSound sound) {
        target.playSound(
                target.getLocation(),
                plugin.getSoundConfig().getSound(sound),
                SoundCategory.MASTER,
                getVolume(sound),
                getPitch(sound)
        );
    }

    private float getVolume(@NotNull BoxSound sound) {
        double max = plugin.getSoundConfig().getMaxVolume(sound);
        double min = plugin.getSoundConfig().getMinVolume(sound);

        if (min < max) {
            return (float) random.doubles(min, max).findFirst().orElse(1.0);
        } else {
            return (float) max;
        }
    }

    private float getPitch(@NotNull BoxSound sound) {
        double max = plugin.getSoundConfig().getMaxPitch(sound);
        double min = plugin.getSoundConfig().getMinPitch(sound);

        if (min < max) {
            return (float) random.doubles(min, max).findFirst().orElse(1.0);
        } else {
            return (float) max;
        }
    }
}
