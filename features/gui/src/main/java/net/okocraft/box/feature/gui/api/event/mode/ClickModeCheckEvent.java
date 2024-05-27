package net.okocraft.box.feature.gui.api.event.mode;

import net.kyori.adventure.util.TriState;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.jetbrains.annotations.NotNull;

public class ClickModeCheckEvent extends BoxEvent {

    private final PlayerSession session;
    private final BoxItemClickMode mode;
    private final boolean originalResult;
    private TriState result = TriState.NOT_SET;

    public ClickModeCheckEvent(@NotNull PlayerSession session, @NotNull BoxItemClickMode mode, boolean originalResult) {
        this.session = session;
        this.mode = mode;
        this.originalResult = originalResult;
    }

    public @NotNull PlayerSession getSession() {
        return this.session;
    }

    public @NotNull BoxItemClickMode getMode() {
        return this.mode;
    }

    public boolean getOriginalResult() {
        return this.originalResult;
    }

    public boolean isAllowed() {
        return this.result.toBooleanOrElse(this.originalResult);
    }

    public void setAllowed(boolean result) {
        this.result = result ? TriState.TRUE : TriState.FALSE;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "ClickModeCheckEvent{" +
                "viewer=" + this.session.getViewer() +
                ", mode=" + this.mode +
                ", originalResult=" + this.originalResult +
                ", result=" + this.result +
                '}';
    }

    @Override
    public String toString() {
        return "ClickModeCheckEvent{" +
                "session=" + this.session +
                ", mode=" + this.mode +
                ", originalResult=" + this.originalResult +
                ", result=" + this.result +
                '}';
    }
}
