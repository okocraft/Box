package net.okocraft.box.feature.stick.function.container;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

final class SoundPlayer {

    static void playDepositSound(@NotNull Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_PIG_SADDLE, 100f, 2.0f);
    }

    static void playWithdrawalSound(@NotNull Player target) {
        target.playSound(target.getLocation(), Sound.ENTITY_PIG_SADDLE, 100f, 1.5f);
    }
}
