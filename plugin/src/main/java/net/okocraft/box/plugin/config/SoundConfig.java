package net.okocraft.box.plugin.config;

import com.github.siroshun09.configapi.bukkit.BukkitYaml;
import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.sound.BoxSound;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class SoundConfig extends BukkitYaml {

    private final static String SOUND_PATH = ".sound";
    private final static String PITCH_MAX_PATH = ".pitch-max";
    private final static String PITCH_MIN_PATH = ".pitch-min";
    private final static String VOLUME_MAX_PATH = ".volume-max";
    private final static String VOLUME_MIN_PATH = ".volume-min";

    private final Box plugin;

    public SoundConfig(@NotNull Box plugin) {
        super(getFilePath(plugin));

        if (!isLoaded()) {
            plugin.getLogger().warning("Failed to load sound.yml");
        }

        this.plugin = plugin;
    }

    @NotNull
    public Sound getSound(@NotNull BoxSound sound) {
        String value = getString(sound.getPath() + SOUND_PATH, sound.getDef().toString());
        try {
            return Sound.valueOf(value);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound name:" + value);
            return sound.getDef();
        }
    }

    public double getMaxVolume(@NotNull BoxSound sound) {
        return Math.min(getDouble(sound.getPath() + VOLUME_MAX_PATH, 2.0), 2.0);
    }

    public double getMinVolume(@NotNull BoxSound sound) {
        return Math.max(getDouble(sound.getPath() + VOLUME_MIN_PATH, 0.5), 0.5);
    }

    public double getMaxPitch(@NotNull BoxSound sound) {
        return Math.min(getDouble(sound.getPath() + PITCH_MAX_PATH, 2.0), 2.0);
    }

    public double getMinPitch(@NotNull BoxSound sound) {
        return Math.max(getDouble(sound.getPath() + PITCH_MAX_PATH, 0.5), 0.5);
    }

    public static void exportDefault(@NotNull Box plugin) {
        BukkitYaml yaml = new BukkitYaml(getFilePath(plugin));

        if (!yaml.isLoaded()) {
            throw new IllegalStateException("Could not load sound.yml");
        }

        for (BoxSound sound : BoxSound.values()) {
            yaml.set(sound.getPath() + SOUND_PATH, sound.getDef().toString());
            yaml.set(sound.getPath() + VOLUME_MAX_PATH, 1.0);
            yaml.set(sound.getPath() + VOLUME_MIN_PATH, 0.5);
            yaml.set(sound.getPath() + PITCH_MAX_PATH, 2.0);
            yaml.set(sound.getPath() + PITCH_MIN_PATH, 0.5);
        }

        if (!yaml.save()) {
            throw new IllegalStateException("Could not save sound.yml");
        }
    }

    @NotNull
    public static Path getFilePath(@NotNull Box plugin) {
        return plugin.getDataFolder().toPath().resolve("sound.yml");
    }
}
