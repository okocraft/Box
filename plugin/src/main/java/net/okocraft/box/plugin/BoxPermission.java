package net.okocraft.box.plugin;

import org.jetbrains.annotations.NotNull;

public enum BoxPermission {

    ;

    private final String node;
    private final boolean def;

    BoxPermission(@NotNull String node, boolean def) {
        this.node = node;
        this.def = def;
    }

    @NotNull
    public String getNode() {
        return node;
    }

    public boolean isDefault() {
        return def;
    }
}
