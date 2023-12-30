package net.okocraft.box.feature.gui.api.menu.paginate;

import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import org.jetbrains.annotations.NotNull;

public interface PaginatedMenu extends Menu {

    static @NotNull TypedKey<Integer> createCurrentPageKey(@NotNull String menuName) {
        return TypedKey.of(Integer.class, "current_page:" + menuName);
    }

    int getMaxPage();

    int getIconsPerPage();

    int getCurrentPage(@NotNull PlayerSession session);

    void setCurrentPage(@NotNull PlayerSession session, int page);

}
