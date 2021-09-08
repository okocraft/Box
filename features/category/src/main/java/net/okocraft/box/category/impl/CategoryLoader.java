package net.okocraft.box.category.impl;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.category.model.Category;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;

public class CategoryLoader {

    public static @NotNull CategoryLoader.CategoryLoadResult load(@NotNull YamlConfiguration yaml) {
        var itemManager = BoxProvider.get().getItemManager();
        var items = new ArrayList<>(itemManager.getBoxItemSet());
        var result = new LinkedHashMap<String, BoxCategory>();

        for (var key : yaml.getKeyList()) {
            if (key.equals("others")) {
                continue;
            }

            var category = result.computeIfAbsent(key, BoxCategory::new);

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
                        item -> Categorizer.byTag(item, name -> result.computeIfAbsent(name, BoxCategory::new))
                ))
                .filter(Predicate.not(
                        item -> Categorizer.byMaterial(item, name -> result.computeIfAbsent(name, BoxCategory::new))
                ))
                .forEach(item -> {
                    var categoryName = itemManager.isCustomItem(item) ? "custom-items" : "others";
                    result.computeIfAbsent(categoryName, BoxCategory::new).add(item);
                });

        return new CategoryLoadResult(List.copyOf(result.values()));
    }

    public static record CategoryLoadResult(@NotNull @Unmodifiable List<Category> categoryList) {

        public @NotNull CategoryLoadResult export(@NotNull YamlConfiguration yaml) throws Exception {
            for (var category : categoryList) {
                yaml.set(category.getName(), category.getItems().stream().map(BoxItem::getPlainName).toList());
            }

            yaml.save();

            return this;
        }
    }

    private CategoryLoader() {
        throw new UnsupportedOperationException();
    }
}
