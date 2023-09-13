package net.okocraft.box.feature.gui.api.event;

import com.github.siroshun09.event4j.event.Cancellable;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MenuClickEvent extends MenuEvent implements Cancellable {

    private final Button clickedButton;
    private final ClickType clickType;
    private boolean cancelled;

    public MenuClickEvent(@NotNull Menu menu, @NotNull PlayerSession session,
                          @NotNull Button clickedButton, @NotNull ClickType clickType) {
        super(menu, session);
        this.clickedButton = Objects.requireNonNull(clickedButton);
        this.clickType = Objects.requireNonNull(clickType);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public @NotNull Button getClickedButton() {
        return clickedButton;
    }

    public @NotNull ClickType getClickType() {
        return clickType;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "MenuClickEvent{" +
                "viewerUuid=" + getViewer().getUniqueId() +
                ", viewerName=" + getViewer().getName() +
                ", menuClass=" + getMenu().getClass().getSimpleName() +
                ", buttonClass=" + getClickedButton().getClass().getSimpleName() +
                ", clickType=" + getClickType() +
                ", cancelled=" + isCancelled() +
                '}';
    }

    @Override
    public String toString() {
        return "MenuClickEvent{" +
                "viewer=" + getViewer() +
                ", menu=" + getMenu() +
                ", clickedButton=" + getClickedButton() +
                ", clickType=" + getClickType() +
                ", cancelled=" + isCancelled() +
                '}';
    }
}
