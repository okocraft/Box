package net.okocraft.box.feature.category.internal.file;

import com.github.siroshun09.configapi.api.Configuration;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.category.internal.categorizer.Categorizer;
import net.okocraft.box.feature.category.internal.category.CommonDefaultCategory;
import net.okocraft.box.feature.category.internal.category.DefaultCategory;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.function.Supplier;

public final class CategoryLoader {

    public static void load(@NotNull CategoryRegistry registry, @NotNull Configuration source) {
        var itemManager = BoxProvider.get().getItemManager();
        var uncategorizedItems = new HashSet<>(itemManager.getBoxItemSet());

        for (var key : source.getKeyList()) {
            if (key.equals("icons") ||
                    key.equals(CommonDefaultCategory.UNCATEGORIZED.getName()) ||
                    key.equals(CommonDefaultCategory.CUSTOM_ITEMS.getName())) {
                continue;
            }

            Material iconMaterial;

            var iconMaterialName = source.getString("icons." + key).toUpperCase(Locale.ROOT);

            try {
                iconMaterial = Material.valueOf(iconMaterialName);
            } catch (IllegalArgumentException e) {
                BoxProvider.get().getLogger().warning("Unknown icon: " + iconMaterialName + " (icons." + key + ")");
                iconMaterial = Material.STONE;
            }

            var category = getOrCreateCategory(registry, key, iconMaterial);

            var itemNameList = source.getStringList(key);

            for (var itemName : itemNameList) {
                var optionalBoxItem = itemManager.getBoxItem(itemName);

                if (optionalBoxItem.isPresent()) {
                    category.addItem(optionalBoxItem.get());
                    uncategorizedItems.remove(optionalBoxItem.get());
                }
            }
        }

        for (var item : uncategorizedItems.stream().sorted(Comparator.comparing(BoxItem::getPlainName)).toList()) {
            var defaultCategory = Categorizer.categorize(item.getOriginal());

            if (defaultCategory == null) {
                defaultCategory = itemManager.isCustomItem(item) ?
                        CommonDefaultCategory.CUSTOM_ITEMS :
                        CommonDefaultCategory.UNCATEGORIZED;
            }

            var category = getOrCreateCategory(registry, defaultCategory);
            category.addItem(item);

            BoxProvider.get().getLogger().info("Added uncategorized item '" + item.getPlainName() + "' to category " + defaultCategory.getName());
        }
    }

    private static @NotNull Category getOrCreateCategory(@NotNull CategoryRegistry registry, @NotNull String name, @NotNull Material icon) {
        return getOrCreateCategory(registry, name, () -> Category.create(Component.translatable("box.category.name." + name), icon));
    }

    private static @NotNull Category getOrCreateCategory(@NotNull net.okocraft.box.feature.category.api.registry.CategoryRegistry registry, @NotNull DefaultCategory defaultCategory) {
        return getOrCreateCategory(registry, defaultCategory.getName(), defaultCategory::toCategory);
    }

    private static @NotNull Category getOrCreateCategory(@NotNull net.okocraft.box.feature.category.api.registry.CategoryRegistry registry, @NotNull String name, @NotNull Supplier<Category> categorySupplier) {
        var category = registry.getByName(name).orElse(null);

        if (category == null) {
            category = categorySupplier.get();
            registry.register(name, category);
        }

        return category;
    }

    private CategoryLoader() {
        throw new UnsupportedOperationException();
    }
}
