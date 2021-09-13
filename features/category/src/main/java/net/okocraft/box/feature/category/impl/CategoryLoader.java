package net.okocraft.box.feature.category.impl;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.model.Category;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class CategoryLoader {

    public static @NotNull CategoryLoader.CategoryLoadResult load(@NotNull YamlConfiguration yaml) {
        var itemManager = BoxProvider.get().getItemManager();
        var items = new ArrayList<>(itemManager.getBoxItemSet());
        var result = new LinkedHashMap<String, BoxCategory>();

        for (var key : yaml.getKeyList()) {
            if (key.equals("icons") ||
                    key.equals(DefaultCategory.UNCATEGORIZED.getName()) ||
                    key.equals(DefaultCategory.CUSTOM_ITEMS.getName())) {
                continue;
            }

            Material temp;

            var iconMaterialName = yaml.getString("icons." + key).toUpperCase(Locale.ROOT);

            try {
                temp = Material.valueOf(iconMaterialName);
            } catch (IllegalArgumentException e) {
                BoxProvider.get().getLogger().warning("Unknown icon: " + iconMaterialName + " (icons." + key + ")");
                temp = Material.STONE;
            }

            var iconMaterial = temp;

            var category = result.computeIfAbsent(key, k -> new BoxCategory(k, iconMaterial));

            for (var itemName : yaml.getStringList(key)) {
                var optionalBoxItem = itemManager.getBoxItem(itemName);

                if (optionalBoxItem.isPresent()) {
                    category.add(optionalBoxItem.get());
                    items.remove(optionalBoxItem.get());
                } else {
                    BoxProvider.get().getLogger().warning("Unknown item name: " + itemName);
                }
            }
        }

        items.stream()
                .sorted(Comparator.comparing(BoxItem::getPlainName))
                .filter(Predicate.not(
                        item -> Categorizer.byTag(
                                item,
                                def -> result.computeIfAbsent(def.getName(), key -> createCategory(def))
                        ))
                )
                .filter(Predicate.not(
                        item -> Categorizer.byMaterial(
                                item,
                                def -> result.computeIfAbsent(def.getName(), key -> createCategory(def))
                        ))
                )
                .forEach(item -> {
                    var category =
                            itemManager.isCustomItem(item) ?
                                    DefaultCategory.CUSTOM_ITEMS :
                                    DefaultCategory.UNCATEGORIZED;

                    result.computeIfAbsent(
                            category.getName(),
                            name -> createCategory(category)
                    ).add(item);
                });

        result.computeIfAbsent(
                DefaultCategory.CUSTOM_ITEMS.getName(),
                name -> createCategory(DefaultCategory.CUSTOM_ITEMS));

        return new CategoryLoadResult(List.copyOf(result.values()));
    }

    public static record CategoryLoadResult(@NotNull @Unmodifiable List<Category> categoryList) {

        public @NotNull CategoryLoadResult export(@NotNull YamlConfiguration yaml) throws Exception {
            for (var category : categoryList) {
                yaml.set("icons." + category.getName(), category.getIconMaterial().name());
                yaml.set(category.getName(), category.getItems().stream().map(BoxItem::getPlainName).toList());
            }

            yaml.save();

            return this;
        }
    }

    private static @NotNull BoxCategory createCategory(@NotNull DefaultCategory defaultCategory) {
        return new BoxCategory(defaultCategory.getName(), defaultCategory.getIconMaterial());
    }

    private CategoryLoader() {
        throw new UnsupportedOperationException();
    }
}
