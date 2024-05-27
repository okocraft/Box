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
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "MenuOpenEvent{" +
                "viewerUuid=" + this.getViewer().getUniqueId() +
                ", viewerName=" + this.getViewer().getName() +
                ", menuClass=" + this.getMenu().getClass().getSimpleName() +
                ", cancelled=" + this.isCancelled() +
                '}';
    }

    @Override
    public String toString() {
        return "MenuOpenEvent{" +
                "viewer=" + this.getViewer() +
                ", menu=" + this.getMenu() +
                ", cancelled=" + this.isCancelled() +
                '}';
    }
}
