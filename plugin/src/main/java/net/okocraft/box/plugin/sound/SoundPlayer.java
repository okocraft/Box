package net.okocraft.box.plugin.sound;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.config.SoundConfig;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * プレイヤーにサウンドを再生するメソッドを提供するクラス。
 *
 * サウンドのピッチやボリュームの計算もここで行われる。
 */
public class SoundPlayer {

    private final SoundConfig config;
    private final Random random;

    public SoundPlayer(@NotNull Box plugin) {
        this.config = new SoundConfig(plugin);
        this.random = new Random();
    }

    /**
     * 対象に音を鳴らす。
     *
     * この音は対象にしか聞こえない。
     *
     * @param target 音を鳴らすプレイヤー
     * @param sound サウンドの種類
     */
    public void play(@NotNull Player target, @NotNull BoxSound sound) {
        target.playSound(
                target.getLocation(),
                config.getSound(sound),
                SoundCategory.MASTER,
                getVolume(sound),
                getPitch(sound)
        );
    }

    private float getVolume(@NotNull BoxSound sound) {
        double max = config.getMaxVolume(sound);
        double min = config.getMinVolume(sound);

        if (min < max) {
            return (float) random.doubles(min, max).findFirst().orElse(1.0);
        } else {
            return (float) max;
        }
    }

    private float getPitch(@NotNull BoxSound sound) {
        double max = config.getMaxPitch(sound);
        double min = config.getMinPitch(sound);

        if (min < max) {
            return (float) random.doubles(min, max).findFirst().orElse(1.0);
        } else {
            return (float) max;
        }
    }
}
