package net.okocraft.box.item;

import org.jetbrains.annotations.NotNull;

public interface NamedItem<T> {

    @NotNull String plainName();

    @NotNull T item();

}
