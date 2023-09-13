package net.okocraft.box.feature.bemode.listener;

import com.github.siroshun09.event4j.listener.Listener;
import net.okocraft.box.feature.bemode.util.BEPlayerChecker;
import net.okocraft.box.feature.gui.api.event.MenuOpenEvent;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MenuOpenListener implements Listener<MenuOpenEvent> {

    @Override
    public void handle(@NotNull MenuOpenEvent event) {
        var viewer = event.getViewer();

        if (!BEPlayerChecker.isBEPlayer(viewer)) {
            return;
        }

        var session = event.getSession();

        var currentAvailableModes = session.getAvailableClickModes();
        var newModes = new ArrayList<BoxItemClickMode>(currentAvailableModes.size() - 1);

        currentAvailableModes.forEach(mode -> {
            if (mode != ClickModeRegistry.getStorageMode()) {
                newModes.add(mode);
            }
        });

        session.setAvailableClickModes(newModes);
    }
}
