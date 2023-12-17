package net.okocraft.box.feature.gui.api.event;

import net.okocraft.box.api.event.Cancellable;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.jetbrains.annotations.NotNull;

public class MenuOpenEvent extends MenuEvent implements Cancellable {

    private boolean cancelled;

    public MenuOpenEvent(@NotNull Menu menu, @NotNull PlayerSession session) {
        super(menu, session);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "MenuOpenEvent{" +
                "viewerUuid=" + getViewer().getUniqueId() +
                ", viewerName=" + getViewer().getName() +
                ", menuClass=" + getMenu().getClass().getSimpleName() +
                ", cancelled=" + isCancelled() +
                '}';
    }

    @Override
    public String toString() {
        return "MenuOpenEvent{" +
                "viewer=" + getViewer() +
                ", menu=" + getMenu() +
                ", cancelled=" + isCancelled() +
                '}';
    }
}
