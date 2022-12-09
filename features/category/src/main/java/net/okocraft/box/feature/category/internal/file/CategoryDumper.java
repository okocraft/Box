package net.okocraft.box.feature.category.internal.file;

import com.github.siroshun09.configapi.api.Configuration;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import org.jetbrains.annotations.NotNull;

public final class CategoryDumper {

    public static void dump(@NotNull CategoryRegistry registry, @NotNull Configuration target) {
        var categoryMap = registry.asMap();

        for (var entry : categoryMap.entrySet()) {
            var category = entry.getValue();

            if (!category.shouldSave()) {
                return;
            }

            var categoryName = entry.getKey();

            target.set("icons." + categoryName, category.getIconMaterial().name());
            target.set(categoryName, category.getItems().stream().map(BoxItem::getPlainName).toList());
        }
    }
}
