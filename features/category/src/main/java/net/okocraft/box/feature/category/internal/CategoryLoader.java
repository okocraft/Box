package net.okocraft.box.feature.category.internal;

import com.github.siroshun09.configapi.api.Configuration;
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

public class CategoryLoader {

    public static @NotNull @Unmodifiable List<Category> load(@NotNull Configuration yaml) {
        var itemManager = BoxProvider.get().getItemManager();
        var items = new ArrayList<>(itemManager.getBoxItemSet());
        var result = new LinkedHashMap<String, BoxCategory>();
        var itemCategoryMap = new LinkedHashMap<String, List<String>>(); // category name - item list

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

            var itemNameList = yaml.getStringList(key);

            itemCategoryMap.computeIfAbsent(key, k -> new ArrayList<>()).addAll(itemNameList);

            for (var itemName : itemNameList) {
                var optionalBoxItem = itemManager.getBoxItem(itemName);

                var iconMaterial = temp;

                optionalBoxItem.ifPresent(boxItem -> {
                    var category = result.computeIfAbsent(key, k -> new BoxCategory(k, iconMaterial));

                    category.add(boxItem);
                    items.remove(boxItem);
                });
            }
        }

        for (var item : items.stream().sorted(Comparator.comparing(BoxItem::getPlainName)).toList()) {
            BoxProvider.get().getLogger().info("Uncategorized item: " + item.getPlainName());

            var tempCategory = Categorizer.checkTags(item.getOriginal());

            if (tempCategory == null) {
                tempCategory = Categorizer.checkMaterial(item.getOriginal().getType());
            }

            if (tempCategory == null) {
                tempCategory = itemManager.isCustomItem(item) ?
                        DefaultCategory.CUSTOM_ITEMS :
                        DefaultCategory.UNCATEGORIZED;
            }

            var category = tempCategory;

            result.computeIfAbsent(category.getName(), k -> createCategory(category)).add(item);
            itemCategoryMap.computeIfAbsent(category.getName(), k -> new ArrayList<>()).add(item.getPlainName());

            BoxProvider.get().getLogger().info("Added item to category " + category.getName());
        }

        result.computeIfAbsent(
                DefaultCategory.CUSTOM_ITEMS.getName(),
                name -> createCategory(DefaultCategory.CUSTOM_ITEMS));

        // update categories.yml
        for (var entry : itemCategoryMap.entrySet()) {
            yaml.set(entry.getKey(), entry.getValue());
        }

        return List.copyOf(result.values());
    }

    private static @NotNull BoxCategory createCategory(@NotNull DefaultCategory defaultCategory) {
        return new BoxCategory(defaultCategory.getName(), defaultCategory.getIconMaterial());
    }

    private CategoryLoader() {
        throw new UnsupportedOperationException();
    }
}
