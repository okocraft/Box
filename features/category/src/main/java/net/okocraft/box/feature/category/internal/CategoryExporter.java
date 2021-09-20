package net.okocraft.box.feature.category.internal;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.CategoryHolder;
import org.jetbrains.annotations.NotNull;

public final class CategoryExporter {

    public static void export(@NotNull YamlConfiguration yaml) {
        for (var category : CategoryHolder.get()) {
            yaml.set("icons." + category.getName(), category.getIconMaterial().name());
            yaml.set(category.getName(), category.getItems().stream().map(BoxItem::getPlainName).toList());
        }
    }
}
