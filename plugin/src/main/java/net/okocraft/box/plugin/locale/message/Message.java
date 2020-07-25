package net.okocraft.box.plugin.locale.message;

import org.jetbrains.annotations.NotNull;

public enum Message {

    PREFIX("prefix", "&8[&6Box&8]&r "),
    ;

    private final String path;
    private final String def;

    Message(@NotNull String path, @NotNull String def) {
        this.path = path;
        this.def = def;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public String getDefault() {
        return def;
    }
}
