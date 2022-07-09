package net.okocraft.box.feature.gui.api.event;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.feature.gui.api.menu.Menu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MenuEvent extends BoxEvent {

    private final Player viewer;
    private final Menu menu;

    public MenuEvent(@NotNull Player viewer, @NotNull Menu menu) {
        this.viewer = Objects.requireNonNull(viewer);
        this.menu = Objects.requireNonNull(menu);
    }

    public @NotNull Player getViewer() {
        return viewer;
    }

    public @NotNull Menu getMenu() {
        return menu;
    }

    @Override
    public String toString() {
        return "MenuEvent{" +
                "viewer=" + viewer +
                ", menu=" + menu +
                '}';
    }
}
