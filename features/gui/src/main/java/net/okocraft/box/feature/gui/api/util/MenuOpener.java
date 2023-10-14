package net.okocraft.box.feature.gui.api.util;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.gui.internal.util.XmasChecker;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

public final class MenuOpener {

    public static void open(@NotNull Menu menu, @NotNull PlayerSession session) {
        var holder = new BoxInventoryHolder(menu, session);
        var viewer = session.getViewer();

        BoxProvider.get().getScheduler().runEntityTask(viewer, () -> {
            viewer.openInventory(holder.getInventory());

            if (XmasChecker.isXmas()) {
                viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 100f, 1.8f);
            } else {
                viewer.playSound(viewer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 2.0f);
            }
        });
    }

    public static void open(@NotNull Menu menu, @NotNull PlayerSession session, @NotNull Consumer<UUID> onOpened) {
        var holder = new BoxInventoryHolder(menu, session);
        var viewer = session.getViewer();

        BoxProvider.get().getScheduler().runEntityTask(viewer, () -> {
            viewer.openInventory(holder.getInventory());

            if (XmasChecker.isXmas()) {
                viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 100f, 1.8f);
            } else {
                viewer.playSound(viewer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 2.0f);
            }

            onOpened.accept(viewer.getUniqueId());
        });
    }

    private MenuOpener() {
        throw new UnsupportedOperationException();
    }
}
