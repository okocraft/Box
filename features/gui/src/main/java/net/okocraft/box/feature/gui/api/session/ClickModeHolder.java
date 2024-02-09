package net.okocraft.box.feature.gui.api.session;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.feature.gui.api.event.mode.ClickModeCheckEvent;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ClickModeHolder {

    private static final TypedKey<ClickModeHolder> KEY = TypedKey.of(ClickModeHolder.class, "click_mode_holder");

    public static @NotNull ClickModeHolder getFromSession(@NotNull PlayerSession session) {
        return session.computeDataIfAbsent(KEY, () -> createHolder(session));
    }

    private static @NotNull ClickModeHolder createHolder(@NotNull PlayerSession session) {
        var modes = ClickModeRegistry.getModes();
        var availableModes = new ArrayList<BoxItemClickMode>(modes.size());
        var eventManager = BoxAPI.api().getEventManager();

        for (var mode : modes) {
            var checkEvent = new ClickModeCheckEvent(session, mode, mode.canUse(session));
            eventManager.call(checkEvent);

            if (checkEvent.isAllowed()) {
                availableModes.add(mode);
            }
        }

        if (availableModes.isEmpty()) {
            availableModes.add(ClickModeRegistry.getStorageMode());
        }

        return new ClickModeHolder(availableModes);
    }

    private final List<BoxItemClickMode> availableModes;
    private BoxItemClickMode currentMode;

    private ClickModeHolder(@NotNull List<BoxItemClickMode> availableModes) {
        this.availableModes = availableModes;
        this.currentMode = availableModes.getFirst();
    }

    public @NotNull @Unmodifiable List<BoxItemClickMode> getAvailableModes() {
        return this.availableModes;
    }

    public @NotNull BoxItemClickMode getCurrentMode() {
        return this.currentMode;
    }

    public void setCurrentMode(@NotNull BoxItemClickMode currentMode) {
        this.currentMode = Objects.requireNonNull(currentMode);
    }
}
