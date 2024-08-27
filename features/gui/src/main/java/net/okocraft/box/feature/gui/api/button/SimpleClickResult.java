package net.okocraft.box.feature.gui.api.button;

import org.jetbrains.annotations.NotNull;

final class SimpleClickResult implements ClickResult {

    private final String name;

    SimpleClickResult(@NotNull String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SimpleClickResult{" +
            "name='" + this.name + '\'' +
            '}';
    }
}
