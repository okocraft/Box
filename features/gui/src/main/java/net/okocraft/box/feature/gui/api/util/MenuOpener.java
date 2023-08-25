package net.okocraft.box.feature.gui.api.util;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.gui.internal.util.XmasChecker;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class MenuOpener {

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
            viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 100f, 1.8f);
        } else {
            viewer.playSound(viewer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 2.0f);
        }
    }

    private MenuOpener() {
        throw new UnsupportedOperationException();
    }
}
