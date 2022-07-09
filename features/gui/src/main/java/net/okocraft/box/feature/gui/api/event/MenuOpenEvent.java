package net.okocraft.box.feature.gui.api.event;

import com.github.siroshun09.event4j.event.Cancellable;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.okocraft.box.feature.gui.api.menu.Menu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MenuOpenEvent extends MenuEvent implements Cancellable {

    private boolean cancelled;

    public MenuOpenEvent(@NotNull Player viewer, @NotNull Menu menu) {
        super(viewer, menu);
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
                ", menuTitle=" + PlainTextComponentSerializer.plainText().serialize(getMenu().getTitle()) +
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
