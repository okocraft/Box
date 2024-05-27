package net.okocraft.box.feature.gui.api.event;

import net.okocraft.box.api.event.Cancellable;
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
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public @NotNull Button getClickedButton() {
        return this.clickedButton;
    }

    public @NotNull ClickType getClickType() {
        return this.clickType;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "MenuClickEvent{" +
                "viewerUuid=" + this.getViewer().getUniqueId() +
                ", viewerName=" + this.getViewer().getName() +
                ", menuClass=" + this.getMenu().getClass().getSimpleName() +
                ", buttonClass=" + this.getClickedButton().getClass().getSimpleName() +
                ", clickType=" + this.getClickType() +
                ", cancelled=" + this.isCancelled() +
                '}';
    }

    @Override
    public String toString() {
        return "MenuClickEvent{" +
                "viewer=" + this.getViewer() +
                ", menu=" + this.getMenu() +
                ", clickedButton=" + this.getClickedButton() +
                ", clickType=" + this.getClickType() +
                ", cancelled=" + this.isCancelled() +
                '}';
    }
}
