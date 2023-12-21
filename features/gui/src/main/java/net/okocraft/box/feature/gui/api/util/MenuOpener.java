package net.okocraft.box.feature.gui.api.util;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.gui.internal.util.XmasChecker;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class MenuOpener {

    private static final SoundBase XMAS_SOUND = SoundBase.builder().sound(Sound.BLOCK_NOTE_BLOCK_CHIME).pitch(1.8f).build();
    private static final SoundBase NORMAL_SOUND = SoundBase.builder().sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).pitch(2.0f).build();

    public static void open(@NotNull Menu menu, @NotNull Player viewer) {
        open(menu, viewer, true);
    }

    public static void open(@NotNull Menu menu, @NotNull Player viewer, boolean await) {
        var holder = new BoxInventoryHolder(menu);

        holder.initializeMenu(viewer);
        holder.applyContents();

        var future = BoxProvider.get().getTaskFactory().runEntityTask(viewer, player -> player.openInventory(holder.getInventory()));

        if (await) {
            future.join();
        }

        if (XmasChecker.isXmas()) {
            XMAS_SOUND.play(viewer);
        } else {
            NORMAL_SOUND.play(viewer);
        }
    }

    private MenuOpener() {
        throw new UnsupportedOperationException();
    }
}
