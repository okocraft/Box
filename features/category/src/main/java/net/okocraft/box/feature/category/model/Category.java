package net.okocraft.box.feature.category.model;

import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.Translatable;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface Category {

    @NotNull String getName();

    @NotNull TranslatableComponent getDisplayName();

    @NotNull @Unmodifiable List<BoxItem> getItems();
}
