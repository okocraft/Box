package net.okocraft.box.feature.category.api.category;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface Category {

    static @NotNull Category create(@NotNull Component displayName, @NotNull Material iconMaterial) {
        return create(displayName, iconMaterial, true);
    }

    static @NotNull Category create(@NotNull Component displayName, @NotNull Material iconMaterial, boolean shouldSave) {
        return new CategoryImpl(displayName, iconMaterial, shouldSave);
    }

    @NotNull Component getDisplayName();

    @NotNull Material getIconMaterial();

    @NotNull @Unmodifiable List<BoxItem> getItems();

    void addItem(@NotNull BoxItem item);

    void removeItem(@NotNull BoxItem item);

    boolean shouldSave();
}
