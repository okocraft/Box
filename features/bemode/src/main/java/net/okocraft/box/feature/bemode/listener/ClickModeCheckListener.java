package net.okocraft.box.feature.bemode.listener;

import net.kyori.adventure.key.Key;
import net.okocraft.box.api.model.manager.EventManager;
import net.okocraft.box.feature.bemode.util.BEPlayerChecker;
import net.okocraft.box.feature.gui.api.event.mode.ClickModeCheckEvent;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.jetbrains.annotations.NotNull;

public final class ClickModeCheckListener  {

    private static final Key KEY = Key.key("box", "feature/bemode/listener/click_mode_check_event");

    public static void register(@NotNull EventManager eventManager) {
        eventManager.getSubscriber(ClickModeCheckEvent.class).subscribe(KEY, event -> {
            if (event.getMode() == ClickModeRegistry.getStorageMode() &&
                    BEPlayerChecker.isBEPlayer(event.getSession().getViewer())) {
                event.setAllowed(false);
            }
        });
    }

    public static void unregister(@NotNull EventManager eventManager) {
        eventManager.getSubscriber(ClickModeCheckEvent.class).unsubscribeByKey(KEY);
    }

    private ClickModeCheckListener() {
        throw new UnsupportedOperationException();
    }
}
