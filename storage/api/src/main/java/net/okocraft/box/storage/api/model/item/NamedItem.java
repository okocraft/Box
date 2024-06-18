package net.okocraft.box.storage.api.model.item;

import org.jetbrains.annotations.NotNull;

public interface NamedItem<T> {

    @NotNull String plainName();

    @NotNull T item();

}
