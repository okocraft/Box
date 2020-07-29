package net.okocraft.box.plugin;

import org.jetbrains.annotations.NotNull;

public enum BoxPermission {

    BOX_AUTO_STORE("box.autostore", true),

    BOX_STICK_BREAK("box.stick.break", true),
    BOX_STICK_CONSUME("box.stick.consume", true),
    BOX_STICK_OPEN("box.stick.open", true),
    BOX_STICK_PLACE("box.stick.place", true),
    BOX_STICK_THROW("box.stick.throw", true)
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
