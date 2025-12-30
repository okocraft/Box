package net.okocraft.box.feature.gui.api.session;

import net.okocraft.box.feature.gui.api.menu.Menu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MenuHistoryHolder {

    private static final TypedKey<MenuHistoryHolder> KEY = TypedKey.of(MenuHistoryHolder.class, "menu_history_holder");

    public static @NotNull MenuHistoryHolder getFromSession(@NotNull PlayerSession session) {
        return session.computeDataIfAbsent(KEY, MenuHistoryHolder::new);
    }

    private @Nullable PreviousMenu previousMenu;

    private MenuHistoryHolder() {
    }

    public boolean hasPreviousMenu() {
        return this.previousMenu != null;
    }

    public @NotNull Menu backMenu() {
        if (this.previousMenu == null) {
            throw new IllegalStateException("No previous menu.");
        }

        Menu menu = this.previousMenu.menu;
        this.previousMenu = this.previousMenu.parent;

        return menu;
    }

    public void rememberMenu(@NotNull Menu menu) {
        this.previousMenu = new PreviousMenu(menu, this.previousMenu);
    }

    private record PreviousMenu(@NotNull Menu menu, @Nullable PreviousMenu parent) {
    }
}
