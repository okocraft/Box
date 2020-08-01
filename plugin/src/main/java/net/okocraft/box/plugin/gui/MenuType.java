package net.okocraft.box.plugin.gui;

public enum MenuType {
    CATEGORY_SELECTOR(54);

    private final int size;

    MenuType(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
