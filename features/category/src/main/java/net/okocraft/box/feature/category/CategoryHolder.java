package net.okocraft.box.feature.category;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.category.model.Category;
import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * @deprecated use {@link CategoryRegistry}
 */
@Deprecated(forRemoval = true, since = "5.1.0")
@ApiStatus.ScheduledForRemoval(inVersion = "5.2.0")
@SuppressWarnings("removal")
public final class CategoryHolder {

    /**
     * @deprecated use {@link CategoryRegistry#values()}
     */
    @Deprecated(forRemoval = true, since = "5.1.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.2.0")
    public static @NotNull @Unmodifiable List<Category> get() {
        return CategoryRegistry.get().values().stream().map(OldCategory::new).map(Category.class::cast).toList();
    }

    @SuppressWarnings("removal")
    private record OldCategory(
            @NotNull net.okocraft.box.feature.category.api.category.Category category) implements Category {

        @Override
        public @NotNull String getName() {
            return CategoryRegistry.get().getRegisteredName(category);
        }

        @Override
        public @NotNull TranslatableComponent getDisplayName() {
            if (category.getDisplayName() instanceof TranslatableComponent translatableComponent) {
                return translatableComponent;
            } else {
                // Since the return value of this method is TranslatableComponent, it is not fully compatible.
                return Component.translatable("box.category.name." + getName());
            }
        }

        @Override
        public @NotNull Material getIconMaterial() {
            return category.getIconMaterial();
        }

        @Override
        public @NotNull @Unmodifiable List<BoxItem> getItems() {
            return category.getItems();
        }
    }
}
