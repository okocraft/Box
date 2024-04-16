package net.okocraft.box.feature.category.internal.category.defaults;

import com.github.siroshun09.configapi.core.node.BooleanValue;
import com.github.siroshun09.configapi.core.node.MapNode;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.internal.category.LoadedCategory;
import net.okocraft.box.feature.category.internal.file.CategoryFile;
import org.bukkit.Material;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record DefaultCategory(@NotNull String key,
                              @NotNull Material icon,
                              @NotNull Map<Locale, String> displayNameMap,
                              @NotNull List<String> itemNames) {

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public @NotNull Category toCategory(@NotNull ItemManager manager) {
        var category = new LoadedCategory(this.icon, this.displayNameMap);
        category.addItems(this.itemNames.stream().map(manager::getBoxItem).filter(Optional::isPresent).map(Optional::get).toList());
        return category;
    }

    public void storeToMapNode(@NotNull MapNode mapNode) {
        mapNode.set(CategoryFile.DISABLED_CATEGORY, BooleanValue.FALSE);
        mapNode.set(CategoryFile.ICON_KEY, this.icon.name());

        var displayName = mapNode.createMap(CategoryFile.DISPLAY_NAME_KEY);

        for (var entry : this.displayNameMap.entrySet()) {
            displayName.set(entry.getKey() == null ? CategoryFile.LOCALE_DEFAULT : entry.getKey().toString(), entry.getValue());
        }

        mapNode.set(CategoryFile.ITEMS_KEY, this.itemNames);
    }

    public static class Builder {

        private final Map<Locale, String> displayNameMap = new LinkedHashMap<>();

        private String key;
        private Material icon;
        private List<String> itemNames;

        private Builder() {
        }

        @Contract("_ -> this")
        public @NotNull Builder key(@NotNull String key) {
            this.key = key;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder icon(@NotNull Material icon) {
            this.icon = icon;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder addDefaultDisplayName(@NotNull String displayName) {
            this.displayNameMap.put(null, displayName);
            return this;
        }

        @Contract("_, _ -> this")
        public @NotNull Builder addDisplayName(@NotNull Locale locale, @NotNull String displayName) {
            this.displayNameMap.put(locale, displayName);
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder items(List<String> names) {
            this.itemNames = Objects.requireNonNullElseGet(names, Collections::emptyList);
            return this;
        }

        @Contract("-> new")
        public @NotNull DefaultCategory build() {
            return new DefaultCategory(this.key, this.icon, Collections.unmodifiableMap(this.displayNameMap), Collections.unmodifiableList(this.itemNames));
        }
    }
}
