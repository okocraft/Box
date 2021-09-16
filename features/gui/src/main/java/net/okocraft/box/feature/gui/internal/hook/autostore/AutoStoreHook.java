package net.okocraft.box.feature.gui.internal.hook.autostore;

import com.github.siroshun09.event4j.handlerlist.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.feature.FeatureEvent;
import net.okocraft.box.feature.autostore.AutoStoreFeature;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.jetbrains.annotations.NotNull;

public final class AutoStoreHook {

    private static final Key LISTENER_KEY = Key.of("box_gui:autostore_hook");
    private static BoxItemClickMode REGISTERED_MODE;

    public static void enableIfRegistered() {
        for (var feature : BoxProvider.get().getFeatures()) {
            if (feature instanceof AutoStoreFeature autoStoreFeature) {
                enable(autoStoreFeature);
            }
        }

    }

    private static void enable(@NotNull AutoStoreFeature feature) {
        REGISTERED_MODE = new AutoStoreClickMode(feature.getSettingManager());

        ClickModeRegistry.register(REGISTERED_MODE);
    }

    public static void disable() {
        if (REGISTERED_MODE != null) {
            ClickModeRegistry.unregister(REGISTERED_MODE);
        }
    }

    public static void registerListener() {
        BoxProvider.get().getEventBus().getHandlerList(FeatureEvent.class)
                .subscribe(LISTENER_KEY, AutoStoreHook::processEvent);
    }

    public static void unregisterListener() {
        BoxProvider.get().getEventBus().unsubscribeAll(LISTENER_KEY);
    }

    private static void processEvent(@NotNull FeatureEvent event) {
        if (!(event.getFeature() instanceof AutoStoreFeature feature)) {
            return;
        }

        if (REGISTERED_MODE == null && event.getType() == FeatureEvent.Type.REGISTER) {
            enable(feature);
            return;
        }

        if (REGISTERED_MODE != null && event.getType() == FeatureEvent.Type.UNREGISTER) {
            disable();
        }
    }
}
