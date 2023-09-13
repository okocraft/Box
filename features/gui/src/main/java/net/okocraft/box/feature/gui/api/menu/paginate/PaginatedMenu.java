package net.okocraft.box.feature.gui.api.menu.paginate;

import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import org.jetbrains.annotations.NotNull;

public interface PaginatedMenu extends Menu {

    TypedKey<Integer> CURRENT_PAGE_KEY = TypedKey.of(Integer.class, "current_page");

    static int getCurrentPage(@NotNull PlayerSession session) {
        Integer data = session.getData(CURRENT_PAGE_KEY);
        return data != null ? data : 0;
    }

    int getMaxPage();

    int getIconsPerPage();

}
