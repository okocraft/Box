package net.okocraft.box.gui.api.menu.paginate;

import net.okocraft.box.gui.api.menu.Menu;

public interface PaginatedMenu extends Menu {

    int getCurrentPage();

    boolean hasNext();

    boolean hasPrevious();

    void previous();

    void next();

}
