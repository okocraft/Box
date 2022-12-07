package net.okocraft.box.feature.category.model;

import net.kyori.adventure.text.TranslatableComponent;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * @deprecated use {@link net.okocraft.box.feature.category.api.category.Category}
 */
@Deprecated(forRemoval = true, since = "5.1.0")
@ApiStatus.ScheduledForRemoval(inVersion = "5.2.0")
public interface Category {

    /**
     * @deprecated use {@link net.okocraft.box.feature.category.api.registry.CategoryRegistry#getRegisteredName(net.okocraft.box.feature.category.api.category.Category)}
     */
    @Deprecated(forRemoval = true, since = "5.1.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.2.0")
    @NotNull String getName();

    /**
     * @deprecated use {@link net.okocraft.box.feature.category.api.category.Category#getDisplayName()}
     */
    @Deprecated(forRemoval = true, since = "5.1.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.2.0")
    @NotNull TranslatableComponent getDisplayName();

    /**
     * @deprecated use {@link net.okocraft.box.feature.category.api.category.Category#getIconMaterial()}
     */
    @Deprecated(forRemoval = true, since = "5.1.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.2.0")
    @NotNull Material getIconMaterial();

    /**
     * @deprecated use {@link net.okocraft.box.feature.category.api.category.Category#getItems()}
     */
    @Deprecated(forRemoval = true, since = "5.1.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.2.0")
    @NotNull @Unmodifiable List<BoxItem> getItems();
}
