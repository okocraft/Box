package net.okocraft.box.feature.category.internal;

import com.github.siroshun09.configapi.api.Configuration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.internal.category.Categorizer;
import net.okocraft.box.feature.category.internal.category.CommonDefaultCategory;
import net.okocraft.box.feature.category.internal.category.DefaultCategory;
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

        for (var key : yaml.getKeyList()) {
            if (key.equals("icons") ||
                    key.equals(CommonDefaultCategory.UNCATEGORIZED.getName()) ||
                    key.equals(CommonDefaultCategory.CUSTOM_ITEMS.getName())) {
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
            var tempCategory = Categorizer.categorize(item.getOriginal());

            if (tempCategory == null) {
                tempCategory = itemManager.isCustomItem(item) ?
                        CommonDefaultCategory.CUSTOM_ITEMS :
                        CommonDefaultCategory.UNCATEGORIZED;
            }

            var category = tempCategory;

            result.computeIfAbsent(category.getName(), k -> createCategory(category)).add(item);

            BoxProvider.get().getLogger().info("Added uncategorized item '" + item.getPlainName() + "' to category " + category.getName());
        }

        result.computeIfAbsent(
                CommonDefaultCategory.CUSTOM_ITEMS.getName(),
                name -> createCategory(CommonDefaultCategory.CUSTOM_ITEMS)
        );

        // update categories.yml
        yaml.clear();

        for (var category : result.values()) {
            yaml.set("icons." + category.getName(), category.getIconMaterial().name());
            yaml.set(category.getName(), category.getItems().stream().map(BoxItem::getPlainName).toList());
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
