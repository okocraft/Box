package net.okocraft.box.feature.gui.api.event;

import com.github.siroshun09.event4j.event.Cancellable;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.menu.RenderedButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MenuClickEvent extends MenuEvent implements Cancellable {

    private final RenderedButton clickedButton;
    private final ClickType clickType;
    private boolean cancelled;

    public MenuClickEvent(@NotNull Player viewer, @NotNull Menu menu,
                          @NotNull RenderedButton clickedButton, @NotNull ClickType clickType) {
        super(viewer, menu);
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

    public @NotNull RenderedButton getClickedButton() {
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
                ", menuTitle=" + PlainTextComponentSerializer.plainText().serialize(getMenu().getTitle()) +
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
