package net.okocraft.box.feature.gui.api.event;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MenuEvent extends BoxEvent {

    private final Menu menu;
    private final PlayerSession session;

    public MenuEvent(@NotNull Menu menu, @NotNull PlayerSession session) {
        this.menu = Objects.requireNonNull(menu);
        this.session = Objects.requireNonNull(session);
    }

    public @NotNull Menu getMenu() {
        return this.menu;
    }

    public @NotNull PlayerSession getSession() {
        return this.session;
    }

    public @NotNull Player getViewer() {
        return this.session.getViewer();
    }
}
