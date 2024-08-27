package net.okocraft.box.feature.bemode.listener;

import net.kyori.adventure.key.Key;
import net.okocraft.box.api.util.SubscribedListenerHolder;
import net.okocraft.box.feature.bemode.util.BEPlayerChecker;
import net.okocraft.box.feature.gui.api.event.mode.ClickModeCheckEvent;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;

public final class ClickModeCheckListener  {

    private static final Key KEY = Key.key("box", "feature/bemode/listener/click_mode_check_event");
    private static final SubscribedListenerHolder LISTENER_HOLDER = new SubscribedListenerHolder();

    public static void register() {
        LISTENER_HOLDER.subscribeAll(subscriber -> subscriber.add(ClickModeCheckEvent.class, KEY, event -> {
            if (event.getMode() == ClickModeRegistry.getStorageMode() &&
                    BEPlayerChecker.isBEPlayer(event.getSession().getViewer())) {
                event.setAllowed(false);
            }
        }));
    }

    public static void unregister() {
        LISTENER_HOLDER.unsubscribeAll();
    }

    private ClickModeCheckListener() {
        throw new UnsupportedOperationException();
    }
}
