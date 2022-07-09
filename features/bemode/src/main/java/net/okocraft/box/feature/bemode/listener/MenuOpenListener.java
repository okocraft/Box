package net.okocraft.box.feature.bemode.listener;

import com.github.siroshun09.event4j.listener.Listener;
import net.okocraft.box.feature.bemode.util.BEPlayerChecker;
import net.okocraft.box.feature.gui.api.event.MenuOpenEvent;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MenuOpenListener implements Listener<MenuOpenEvent> {

    @Override
    public void handle(@NotNull MenuOpenEvent event) {
        var viewer = event.getViewer();

        if (BEPlayerChecker.isBEPlayer(viewer)) {
            var session = PlayerSession.get(viewer);

            var copiedModes = new ArrayList<>(session.getAvailableClickModes());
            copiedModes.remove(ClickModeRegistry.getStorageMode());
            session.setAvailableClickModes(copiedModes);
        }
    }
}
