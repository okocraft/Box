package net.okocraft.box.feature.gui.api.menu.paginate;

import net.okocraft.box.feature.gui.api.menu.Menu;

public interface PaginatedMenu extends Menu {

    int getCurrentPage();

    boolean hasNext();

    boolean hasPrevious();

    void previous();

    void next();

    void setPage(int page);

    int getIconsPerPage();

}
