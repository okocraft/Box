package net.okocraft.box.category.model;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface Category {

    @NotNull String getName();

    @NotNull @Unmodifiable List<BoxItem> getItems();
}
